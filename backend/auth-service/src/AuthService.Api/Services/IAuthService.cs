using System.Threading.Tasks;
using AuthService.Api.Dtos.Auth;
using AuthService.Api.Dtos.Common;

namespace AuthService.Api.Services;

public interface IAuthService
{
    Task<ApiResponse<RegisterResponseDto>> RegisterAsync(RegisterRequestDto request);
    Task<ApiResponse<LoginResponseDto>> LoginAsync(LoginRequestDto request);
    Task<ApiResponse<LoginResponseDto>> GoogleLoginAsync(GoogleLoginRequestDto request);
    Task<ApiResponse<LoginResponseDto>> RefreshTokenAsync(string incomingRefreshToken);
    Task<ApiResponse<bool>> ForgotPasswordAsync(ForgotPasswordRequestDto request);
    Task<ApiResponse<bool>> ResetPasswordAsync(ResetPasswordRequestDto request);
    Task<ApiResponse<bool>> LogoutAsync(int userId);
}
