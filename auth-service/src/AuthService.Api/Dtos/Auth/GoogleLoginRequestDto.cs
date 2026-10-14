namespace AuthService.Api.Dtos.Auth;

public class GoogleLoginRequestDto
{
    // ID Token do Google trả về sau khi user đồng ý đăng nhập Google
    public string IdToken { get; set; } = string.Empty;
}
