using System.Threading.Tasks;
using AuthService.Api.Dtos.Common;
using AuthService.Api.Dtos.User;

namespace AuthService.Api.Services;

public interface IUserService
{
    Task<ApiResponse<UserProfileResponseDto>> GetProfileAsync(int userId);
    Task<ApiResponse<UserProfileResponseDto>> UpdateProfileAsync(int userId, UpdateProfileRequestDto request);
    Task<ApiResponse<bool>> ChangePasswordAsync(int userId, ChangePasswordRequestDto request);
}
