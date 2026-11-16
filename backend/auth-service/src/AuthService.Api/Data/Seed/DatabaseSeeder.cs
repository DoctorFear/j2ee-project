using System.Collections.Generic;
using System.Threading.Tasks;
using Microsoft.EntityFrameworkCore;
using AuthService.Api.Entities;
using AuthService.Api.Services;

namespace AuthService.Api.Data.Seed;

public static class DatabaseSeeder
{
    public static async Task SeedAsync(AuthDbContext context, IPasswordHasher passwordHasher)
    {
        var seedUsers = new List<User>
        {
            // 5 Tài khoản Quản trị viên (Admin) - Mật khẩu: Admin@123
            new User
            {
                Username = "admin",
                Email = "admin@futabus.vn",
                FullName = "Quản trị viên Hệ thống (Admin 1)",
                PhoneNumber = "0901234501",
                PasswordHash = passwordHasher.HashPassword("Admin@123"),
                Role = UserRole.Admin
            },
            new User
            {
                Username = "admin2",
                Email = "admin2@futabus.vn",
                FullName = "Quản trị viên Điều phối 2",
                PhoneNumber = "0901234502",
                PasswordHash = passwordHasher.HashPassword("Admin@123"),
                Role = UserRole.Admin
            },
            new User
            {
                Username = "admin3",
                Email = "admin3@futabus.vn",
                FullName = "Quản trị viên Giám sát 3",
                PhoneNumber = "0901234503",
                PasswordHash = passwordHasher.HashPassword("Admin@123"),
                Role = UserRole.Admin
            },
            new User
            {
                Username = "admin4",
                Email = "admin4@futabus.vn",
                FullName = "Quản trị viên Vận hành 4",
                PhoneNumber = "0901234504",
                PasswordHash = passwordHasher.HashPassword("Admin@123"),
                Role = UserRole.Admin
            },
            new User
            {
                Username = "admin5",
                Email = "admin5@futabus.vn",
                FullName = "Quản trị viên Báo cáo 5",
                PhoneNumber = "0901234505",
                PasswordHash = passwordHasher.HashPassword("Admin@123"),
                Role = UserRole.Admin
            },

            // 5 Tài khoản Khách hàng (Member) - Mật khẩu: Member@123
            new User
            {
                Username = "member1",
                Email = "member1@futabus.vn",
                FullName = "Khách hàng Nguyễn Văn An",
                PhoneNumber = "0902345601",
                PasswordHash = passwordHasher.HashPassword("Member@123"),
                Role = UserRole.Member
            },
            new User
            {
                Username = "member2",
                Email = "member2@futabus.vn",
                FullName = "Khách hàng Trần Thị Bình",
                PhoneNumber = "0902345602",
                PasswordHash = passwordHasher.HashPassword("Member@123"),
                Role = UserRole.Member
            },
            new User
            {
                Username = "member3",
                Email = "member3@futabus.vn",
                FullName = "Khách hàng Lê Hoàng Cường",
                PhoneNumber = "0902345603",
                PasswordHash = passwordHasher.HashPassword("Member@123"),
                Role = UserRole.Member
            },
            new User
            {
                Username = "member4",
                Email = "member4@futabus.vn",
                FullName = "Khách hàng Phạm Thu Duyên",
                PhoneNumber = "0902345604",
                PasswordHash = passwordHasher.HashPassword("Member@123"),
                Role = UserRole.Member
            },
            new User
            {
                Username = "member5",
                Email = "member5@futabus.vn",
                FullName = "Khách hàng Vũ Đức Em",
                PhoneNumber = "0902345605",
                PasswordHash = passwordHasher.HashPassword("Member@123"),
                Role = UserRole.Member
            },

            // 5 Tài khoản Tài xế (Driver) đồng bộ với 5 tài xế đầu trong Admin Transport - Mật khẩu: Driver@123
            new User
            {
                Username = "driver1",
                Email = "driver1@futabus.vn",
                FullName = "Nguyễn Văn Minh 1",
                PhoneNumber = "0909000001",
                PasswordHash = passwordHasher.HashPassword("Driver@123"),
                Role = UserRole.Driver
            },
            new User
            {
                Username = "driver2",
                Email = "driver2@futabus.vn",
                FullName = "Trần Quốc Hùng 1",
                PhoneNumber = "0909000002",
                PasswordHash = passwordHasher.HashPassword("Driver@123"),
                Role = UserRole.Driver
            },
            new User
            {
                Username = "driver3",
                Email = "driver3@futabus.vn",
                FullName = "Lê Hoàng Nam 1",
                PhoneNumber = "0909000003",
                PasswordHash = passwordHasher.HashPassword("Driver@123"),
                Role = UserRole.Driver
            },
            new User
            {
                Username = "driver4",
                Email = "driver4@futabus.vn",
                FullName = "Phạm Thanh Sơn 1",
                PhoneNumber = "0909000004",
                PasswordHash = passwordHasher.HashPassword("Driver@123"),
                Role = UserRole.Driver
            },
            new User
            {
                Username = "driver5",
                Email = "driver5@futabus.vn",
                FullName = "Võ Đức Thành 1",
                PhoneNumber = "0909000005",
                PasswordHash = passwordHasher.HashPassword("Driver@123"),
                Role = UserRole.Driver
            }
        };

        bool hasChanges = false;
        foreach (var user in seedUsers)
        {
            var existing = await context.Users.FirstOrDefaultAsync(u => u.Username == user.Username);
            if (existing == null)
            {
                await context.Users.AddAsync(user);
                hasChanges = true;
            }
            else if (user.Role == UserRole.Driver &&
                     (existing.FullName != user.FullName || existing.PhoneNumber != user.PhoneNumber))
            {
                existing.FullName = user.FullName;
                existing.PhoneNumber = user.PhoneNumber;
                hasChanges = true;
            }
        }

        if (hasChanges)
        {
            await context.SaveChangesAsync();
        }
    }
}
