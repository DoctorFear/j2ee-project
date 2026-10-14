using System;
using System.Text.RegularExpressions;
using System.Threading.Tasks;
using AuthService.Api.Dtos.Common;
using AuthService.Api.Dtos.User;
using AuthService.Api.Repositories;

namespace AuthService.Api.Services;

public class UserService : IUserService
{
    private readonly IUserRepository _userRepository;
    private readonly IPasswordHasher _passwordHasher;

    public UserService(IUserRepository userRepository, IPasswordHasher passwordHasher)
    {
        _userRepository = userRepository;
        _passwordHasher = passwordHasher;
    }

    public async Task<ApiResponse<UserProfileResponseDto>> GetProfileAsync(int userId)
    {
        var user = await _userRepository.GetByIdAsync(userId);
        if (user == null)
        {
            return ApiResponse<UserProfileResponseDto>.Fail("Không tìm thấy người dùng.");
        }

        var response = new UserProfileResponseDto
        {
            Id = user.Id,
            Username = user.Username,
            Email = user.Email,
            FullName = user.FullName,
            PhoneNumber = user.PhoneNumber ?? string.Empty,
            Role = user.Role.ToString(),
            HasPassword = user.HasPassword
        };

        return ApiResponse<UserProfileResponseDto>.Ok(response);
    }

    public async Task<ApiResponse<UserProfileResponseDto>> UpdateProfileAsync(int userId, UpdateProfileRequestDto request)
    {
        if (string.IsNullOrWhiteSpace(request.FullName))
        {
            return ApiResponse<UserProfileResponseDto>.Fail("Họ và tên không được để trống.");
        }

        if (string.IsNullOrWhiteSpace(request.Email))
        {
            return ApiResponse<UserProfileResponseDto>.Fail("Email không được để trống.");
        }

        if (!Regex.IsMatch(request.Email, @"^[^@\s]+@[^@\s]+\.[^@\s]+$"))
        {
            return ApiResponse<UserProfileResponseDto>.Fail("Địa chỉ email không đúng định dạng.");
        }

        if (!string.IsNullOrWhiteSpace(request.PhoneNumber) && !Regex.IsMatch(request.PhoneNumber, @"^(0[3|5|7|8|9])[0-9]{8}$"))
        {
            return ApiResponse<UserProfileResponseDto>.Fail("Số điện thoại không hợp lệ (phải đủ 10 chữ số chuẩn Việt Nam).");
        }

        // Kiểm tra email trùng với user khác
        var existingEmailUser = await _userRepository.GetByEmailAsync(request.Email.Trim().ToLower());
        if (existingEmailUser != null && existingEmailUser.Id != userId)
            return ApiResponse<UserProfileResponseDto>.Fail("Email này đã được sử dụng bởi tài khoản khác.");

        var user = await _userRepository.GetByIdAsync(userId);
        if (user == null)
        {
            return ApiResponse<UserProfileResponseDto>.Fail("Không tìm thấy người dùng.");
        }

        user.FullName = request.FullName.Trim();
        user.Email = request.Email.Trim().ToLower();
        user.PhoneNumber = request.PhoneNumber?.Trim();
        user.UpdatedAt = DateTime.UtcNow;

        await _userRepository.SaveChangesAsync();

        var response = new UserProfileResponseDto
        {
            Id = user.Id,
            Username = user.Username,
            Email = user.Email,
            FullName = user.FullName,
            PhoneNumber = user.PhoneNumber ?? string.Empty,
            Role = user.Role.ToString(),
            HasPassword = user.HasPassword
        };

        return ApiResponse<UserProfileResponseDto>.Ok(response, "Cập nhật hồ sơ thành công!");
    }

    public async Task<ApiResponse<bool>> ChangePasswordAsync(int userId, ChangePasswordRequestDto request)
    {
        var user = await _userRepository.GetByIdAsync(userId);
        if (user == null)
        {
            return ApiResponse<bool>.Fail("Không tìm thấy người dùng.");
        }

        if (string.IsNullOrWhiteSpace(request.NewPassword) ||
            string.IsNullOrWhiteSpace(request.ConfirmNewPassword))
        {
            return ApiResponse<bool>.Fail("Vui lòng điền đầy đủ mật khẩu mới và xác nhận mật khẩu.");
        }

        if (request.NewPassword != request.ConfirmNewPassword)
        {
            return ApiResponse<bool>.Fail("Mật khẩu xác nhận không trùng khớp.");
        }

        if (request.NewPassword.Length < 6)
        {
            return ApiResponse<bool>.Fail("Mật khẩu mới phải từ 6 ký tự trở lên.");
        }

        if (user.HasPassword)
        {
            // User đã có mật khẩu: bắt buộc phải cung cấp mật khẩu hiện tại
            if (string.IsNullOrWhiteSpace(request.CurrentPassword))
            {
                return ApiResponse<bool>.Fail("Vui lòng nhập mật khẩu hiện tại.");
            }

            if (!_passwordHasher.VerifyPassword(request.CurrentPassword, user.PasswordHash!))
            {
                return ApiResponse<bool>.Fail("Mật khẩu hiện tại không chính xác.");
            }

            if (request.NewPassword == request.CurrentPassword)
            {
                return ApiResponse<bool>.Fail("Mật khẩu mới phải khác mật khẩu hiện tại.");
            }
        }
        // Nếu HasPassword == false (user Google): bỏ qua kiểm tra CurrentPassword

        user.PasswordHash = _passwordHasher.HashPassword(request.NewPassword);
        user.HasPassword = true;
        user.UpdatedAt = DateTime.UtcNow;

        await _userRepository.SaveChangesAsync();

        return ApiResponse<bool>.Ok(true, "Đổi mật khẩu thành công!");
    }
}
