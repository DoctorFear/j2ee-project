using System.Threading.Tasks;
using Microsoft.EntityFrameworkCore;
using AuthService.Api.Data;
using AuthService.Api.Entities;

namespace AuthService.Api.Repositories;

public class UserRepository : IUserRepository
{
    private readonly AuthDbContext _context;

    public UserRepository(AuthDbContext context)
    {
        _context = context;
    }

    public async Task<User?> GetByIdAsync(int id)
    {
        return await _context.Users.FirstOrDefaultAsync(u => u.Id == id);
    }

    public async Task<User?> GetByUsernameAsync(string username)
    {
        return await _context.Users.FirstOrDefaultAsync(u => u.Username.ToLower() == username.Trim().ToLower());
    }

    public async Task<User?> GetByRefreshTokenAsync(string refreshToken)
    {
        return await _context.Users.FirstOrDefaultAsync(u => u.RefreshToken == refreshToken);
    }

    public async Task<User?> GetByResetTokenAsync(string resetToken)
    {
        return await _context.Users.FirstOrDefaultAsync(u => u.PasswordResetToken == resetToken);
    }

    public async Task<bool> ExistsByUsernameAsync(string username)
    {
        return await _context.Users.AnyAsync(u => u.Username.ToLower() == username.Trim().ToLower());
    }

    public async Task<User?> GetByEmailAsync(string email)
    {
        return await _context.Users.FirstOrDefaultAsync(u => u.Email == email.Trim().ToLower());
    }

    public async Task<User?> GetByGoogleIdAsync(string googleId)
    {
        return await _context.Users.FirstOrDefaultAsync(u => u.GoogleId == googleId);
    }

    public async Task<bool> ExistsByEmailAsync(string email)
    {
        return await _context.Users.AnyAsync(u => u.Email == email.Trim().ToLower());
    }

    public async Task<string> GenerateUniqueUsernameAsync(string baseUsername)
    {
        // Làm sạch: chỉ giữ chữ, số, dấu chấm, dấu gạch dưới
        var clean = System.Text.RegularExpressions.Regex.Replace(baseUsername.ToLower(), @"[^a-z0-9._]", "");
        if (string.IsNullOrEmpty(clean)) clean = "user";

        var candidate = clean;
        var attempts = 0;
        while (await _context.Users.AnyAsync(u => u.Username == candidate))
        {
            attempts++;
            if (attempts <= 3)
                candidate = $"{clean}_{Random.Shared.Next(1000, 9999)}";
            else
                // Fallback tuyệt đối: timestamp ms, không thể trùng
                candidate = $"{clean}_{DateTimeOffset.UtcNow.ToUnixTimeMilliseconds() % 100000}";

            // Nếu attempt quá nhiều mà vẫn trùng (cực kỳ hiếm), dùng Guid ngắn
            if (attempts > 10)
                candidate = $"{clean}_{Guid.NewGuid().ToString("N")[..8]}";
        }
        return candidate;
    }

    public async Task AddAsync(User user)
    {
        await _context.Users.AddAsync(user);
    }

    public async Task SaveChangesAsync()
    {
        await _context.SaveChangesAsync();
    }
}
