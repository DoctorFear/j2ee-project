using System.ComponentModel.DataAnnotations;

namespace AuthService.Api.Dtos.Auth;

public class RefreshTokenRequestDto
{
    [Required(ErrorMessage = "Thiếu Refresh Token trong request.")]
    public string RefreshToken { get; set; } = string.Empty;
}
