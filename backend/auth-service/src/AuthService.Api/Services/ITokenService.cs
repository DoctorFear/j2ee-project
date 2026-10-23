using AuthService.Api.Entities;

namespace AuthService.Api.Services;

public interface ITokenService
{
    string GenerateAccessToken(User user);
    string GenerateRefreshToken();
}
