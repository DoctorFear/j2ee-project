using System;
using System.Threading.Tasks;
using Microsoft.Extensions.Configuration;
using AuthService.Api.Dtos.Auth;
using AuthService.Api.Dtos.Common;
using AuthService.Api.Entities;
using AuthService.Api.Repositories;

namespace AuthService.Api.Services;

public class AuthenticationService : IAuthService
{
    private readonly IUserRepository _userRepository;
    private readonly IPasswordHasher _passwordHasher;
    private readonly ITokenService _tokenService;
    private readonly IEmailService _emailService;
    private readonly IConfiguration _configuration;

    public AuthenticationService(
        IUserRepository userRepository,
        IPasswordHasher passwordHasher,
        ITokenService tokenService,
        IEmailService emailService,
        IConfiguration configuration)
    {
        _userRepository = userRepository;
        _passwordHasher = passwordHasher;
        _tokenService = tokenService;
        _emailService = emailService;
        _configuration = configuration;
    }

    public async Task<ApiResponse<RegisterResponseDto>> RegisterAsync(RegisterRequestDto request)
    {
        // 1. Kiểm tra Username duy nhất
        if (await _userRepository.ExistsByUsernameAsync(request.Username))
        {
            return ApiResponse<RegisterResponseDto>.Fail("Tên đăng nhập đã tồn tại trên hệ thống.");
        }

        // 2. Kiểm tra Email duy nhất
        if (await _userRepository.ExistsByEmailAsync(request.Email.Trim().ToLower()))
        {
            return ApiResponse<RegisterResponseDto>.Fail("Email này đã được sử dụng bởi tài khoản khác.");
        }

        var passwordHash = _passwordHasher.HashPassword(request.Password);

        var user = new User
        {
            Username = request.Username.Trim(),
            Email = request.Email.Trim().ToLower(),
            FullName = request.FullName.Trim(),
            PhoneNumber = request.PhoneNumber?.Trim(),
            PasswordHash = passwordHash,
            Role = UserRole.Member
        };

        await _userRepository.AddAsync(user);
        await _userRepository.SaveChangesAsync();

        var response = new RegisterResponseDto
        {
            UserId = user.Id,
            Username = user.Username,
            Email = user.Email,
            FullName = user.FullName,
            Role = user.Role.ToString(),
            CreatedAt = user.CreatedAt
        };

        return ApiResponse<RegisterResponseDto>.Ok(response, "Đăng ký tài khoản thành công!");
    }

    public async Task<ApiResponse<LoginResponseDto>> LoginAsync(LoginRequestDto request)
    {
        var user = await _userRepository.GetByUsernameAsync(request.Username);

        if (user == null || string.IsNullOrEmpty(user.PasswordHash) || !_passwordHasher.VerifyPassword(request.Password, user.PasswordHash))
        {
            return ApiResponse<LoginResponseDto>.Fail("Tên đăng nhập hoặc mật khẩu không chính xác.");
        }

        var refreshToken = _tokenService.GenerateRefreshToken();
        user.RefreshToken = refreshToken;
        user.RefreshTokenExpiryTime = DateTime.UtcNow.AddDays(3);
        await _userRepository.SaveChangesAsync();

        var accessToken = _tokenService.GenerateAccessToken(user);

        var response = new LoginResponseDto
        {
            UserId = user.Id,
            Username = user.Username,
            Email = user.Email,
            FullName = user.FullName,
            Role = user.Role.ToString(),
            HasPassword = user.HasPassword,
            AccessToken = accessToken,
            RefreshToken = refreshToken
        };

        return ApiResponse<LoginResponseDto>.Ok(response, "Đăng nhập thành công!");
    }

    public async Task<ApiResponse<LoginResponseDto>> RefreshTokenAsync(string incomingRefreshToken)
    {
        var user = await _userRepository.GetByRefreshTokenAsync(incomingRefreshToken);

        if (user == null || user.RefreshTokenExpiryTime <= DateTime.UtcNow)
        {
            return ApiResponse<LoginResponseDto>.Fail("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
        }

        var newRefreshToken = _tokenService.GenerateRefreshToken();
        user.RefreshToken = newRefreshToken;
        user.RefreshTokenExpiryTime = DateTime.UtcNow.AddDays(3);
        await _userRepository.SaveChangesAsync();

        var newAccessToken = _tokenService.GenerateAccessToken(user);

        var response = new LoginResponseDto
        {
            UserId = user.Id,
            Username = user.Username,
            Email = user.Email,
            FullName = user.FullName,
            Role = user.Role.ToString(),
            HasPassword = user.HasPassword,
            AccessToken = newAccessToken,
            RefreshToken = newRefreshToken
        };

        return ApiResponse<LoginResponseDto>.Ok(response, "Làm mới phiên thành công!");
    }

    public async Task<ApiResponse<bool>> ForgotPasswordAsync(ForgotPasswordRequestDto request)
    {
        if (string.IsNullOrWhiteSpace(request.Username) || string.IsNullOrWhiteSpace(request.Email))
        {
            return ApiResponse<bool>.Fail("Tên đăng nhập và Email không được để trống.");
        }

        var user = await _userRepository.GetByUsernameAsync(request.Username);

        if (user == null || !user.Email.Equals(request.Email.Trim(), StringComparison.OrdinalIgnoreCase))
        {
            return ApiResponse<bool>.Ok(true, "Nếu thông tin chính xác, liên kết đặt lại mật khẩu đã được gửi về email của bạn.");
        }

        var resetToken = Guid.NewGuid().ToString("N");
        user.PasswordResetToken = resetToken;
        user.ResetTokenExpires = DateTime.UtcNow.AddMinutes(15);
        await _userRepository.SaveChangesAsync();

        try
        {
            var frontendUrl = _configuration["FrontendUrl"] ?? "http://localhost:5173";
            var resetLink = $"{frontendUrl.TrimEnd('/')}/reset-password?token={resetToken}";
            var emailBody = $"<p>Chào <b>{user.FullName}</b>,</p>" +
                            $"<p>Bạn vừa yêu cầu khôi phục mật khẩu tài khoản FutaBus. Vui lòng nhấn vào liên kết bên dưới để đặt lại mật khẩu (liên kết có hiệu lực trong 15 phút):</p>" +
                            $"<p><a href='{resetLink}' style='background:#f97316;color:#fff;padding:10px 16px;text-decoration:none;border-radius:6px;display:inline-block;'>Đặt lại mật khẩu</a></p>" +
                            $"<p>Nếu bạn không gửi yêu cầu này, vui lòng bỏ qua email.</p>";

            await _emailService.SendEmailAsync(user.Email, "Khôi phục mật khẩu FutaBus", emailBody);
        }
        catch (Exception)
        {
            return ApiResponse<bool>.Fail("Lỗi gián đoạn dịch vụ gửi email. Vui lòng thử lại sau.");
        }

        return ApiResponse<bool>.Ok(true, "Liên kết đặt lại mật khẩu đã được gửi về email của bạn. Vui lòng kiểm tra hộp thư.");
    }

    public async Task<ApiResponse<bool>> ResetPasswordAsync(ResetPasswordRequestDto request)
    {
        var user = await _userRepository.GetByResetTokenAsync(request.Token);
        if (user == null || user.ResetTokenExpires < DateTime.UtcNow)
        {
            return ApiResponse<bool>.Fail("Mã xác nhận đã hết hạn hoặc không hợp lệ. Vui lòng gửi lại yêu cầu.");
        }

        if (string.IsNullOrWhiteSpace(request.NewPassword) || request.NewPassword.Length < 6)
        {
            return ApiResponse<bool>.Fail("Mật khẩu mới phải có tối thiểu 6 ký tự.");
        }

        if (request.NewPassword != request.ConfirmPassword)
        {
            return ApiResponse<bool>.Fail("Mật khẩu xác nhận không trùng khớp.");
        }

        user.PasswordHash = _passwordHasher.HashPassword(request.NewPassword);
        user.HasPassword = true;
        user.PasswordResetToken = null;
        user.ResetTokenExpires = null;
        user.UpdatedAt = DateTime.UtcNow;

        await _userRepository.SaveChangesAsync();

        return ApiResponse<bool>.Ok(true, "Đặt lại mật khẩu thành công! Vui lòng đăng nhập lại.");
    }

    public async Task<ApiResponse<LoginResponseDto>> GoogleLoginAsync(GoogleLoginRequestDto request)
    {
        // 1. Validate Google ID Token thật bằng cách gọi Google tokeninfo endpoint chính thức của Google
        using var http = new HttpClient();
        HttpResponseMessage googleResponse;
        try
        {
            googleResponse = await http.GetAsync($"https://oauth2.googleapis.com/tokeninfo?id_token={request.IdToken}");
        }
        catch
        {
            return ApiResponse<LoginResponseDto>.Fail("Không thể kết nối đến máy chủ Google để xác thực. Vui lòng kiểm tra kết nối mạng.");
        }

        if (!googleResponse.IsSuccessStatusCode)
            return ApiResponse<LoginResponseDto>.Fail("Google ID Token không hợp lệ hoặc đã hết hạn từ máy chủ Google.");

        var json = await googleResponse.Content.ReadAsStringAsync();
        using var doc = System.Text.Json.JsonDocument.Parse(json);
        var root = doc.RootElement;

        var googleId = root.TryGetProperty("sub", out var subEl) ? subEl.GetString() : null;
        var email = root.TryGetProperty("email", out var emailEl) ? emailEl.GetString() : null;
        var fullName = root.TryGetProperty("name", out var nameEl) ? nameEl.GetString() : null;
        var emailVerified = root.TryGetProperty("email_verified", out var verEl) && 
            (verEl.ValueKind == System.Text.Json.JsonValueKind.True || (verEl.ValueKind == System.Text.Json.JsonValueKind.String && verEl.GetString() == "true"));

        if (string.IsNullOrEmpty(googleId) || string.IsNullOrEmpty(email) || !emailVerified)
            return ApiResponse<LoginResponseDto>.Fail("Tài khoản Google chưa xác minh email.");

        // 2. Tìm user theo GoogleId trước
        var user = await _userRepository.GetByGoogleIdAsync(googleId);

        // 3. Nếu chưa có theo GoogleId, thử tìm theo Email
        if (user == null)
        {
            user = await _userRepository.GetByEmailAsync(email);
            if (user != null)
            {
                // Account Linking: email đã tồn tại
                // CHẶN nếu không phải Member (Admin/Driver không dùng Google Login)
                if (user.Role != UserRole.Member)
                    return ApiResponse<LoginResponseDto>.Fail("Tài khoản Quản trị và Tài xế phải đăng nhập bằng Tên đăng nhập hệ thống để đảm bảo bảo mật.");

                // Liên kết Google account vào tài khoản có sẵn
                user.GoogleId = googleId;
                user.UpdatedAt = DateTime.UtcNow;
                await _userRepository.SaveChangesAsync();
            }
        }

        // 4. Nếu vẫn chưa có: Tạo tài khoản Member mới từ Google
        if (user == null)
        {
            var baseUsername = email.Split('@')[0];
            var username = await _userRepository.GenerateUniqueUsernameAsync(baseUsername);

            user = new User
            {
                Username = username,
                Email = email.ToLower(),
                FullName = fullName ?? username,
                GoogleId = googleId,
                PasswordHash = null,
                HasPassword = false,
                Role = UserRole.Member
            };

            try
            {
                await _userRepository.AddAsync(user);
                await _userRepository.SaveChangesAsync();
            }
            catch (Microsoft.EntityFrameworkCore.DbUpdateException)
            {
                // Race condition: thử lại với username khác
                user.Username = await _userRepository.GenerateUniqueUsernameAsync(baseUsername + "_");
                await _userRepository.SaveChangesAsync();
            }
        }

        // 5. Tạo token và trả về
        var refreshToken = _tokenService.GenerateRefreshToken();
        user.RefreshToken = refreshToken;
        user.RefreshTokenExpiryTime = DateTime.UtcNow.AddDays(3);
        await _userRepository.SaveChangesAsync();

        var accessToken = _tokenService.GenerateAccessToken(user);

        return ApiResponse<LoginResponseDto>.Ok(new LoginResponseDto
        {
            UserId = user.Id,
            Username = user.Username,
            Email = user.Email,
            FullName = user.FullName,
            Role = user.Role.ToString(),
            HasPassword = user.HasPassword,
            AccessToken = accessToken,
            RefreshToken = refreshToken
        }, "Đăng nhập Google thành công!");
    }

    public async Task<ApiResponse<bool>> LogoutAsync(int userId)
    {
        var user = await _userRepository.GetByIdAsync(userId);
        if (user == null)
        {
            return ApiResponse<bool>.Fail("Không tìm thấy người dùng.");
        }

        user.RefreshToken = null;
        user.RefreshTokenExpiryTime = null;
        await _userRepository.SaveChangesAsync();

        return ApiResponse<bool>.Ok(true, "Đăng xuất thành công.");
    }
}
