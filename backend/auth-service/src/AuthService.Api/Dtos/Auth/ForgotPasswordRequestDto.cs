using System.ComponentModel.DataAnnotations;

namespace AuthService.Api.Dtos.Auth;

public class ForgotPasswordRequestDto
{
    [Required(ErrorMessage = "Email không được để trống.")]
    [EmailAddress(ErrorMessage = "Địa chỉ email không đúng định dạng.")]
    public string Email { get; set; } = string.Empty;
}
