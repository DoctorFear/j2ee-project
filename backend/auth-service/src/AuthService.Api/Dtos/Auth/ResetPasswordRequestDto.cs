using System.ComponentModel.DataAnnotations;

namespace AuthService.Api.Dtos.Auth;

public class ResetPasswordRequestDto
{
    [Required(ErrorMessage = "Thiếu mã xác nhận đặt lại mật khẩu.")]
    public string Token { get; set; } = string.Empty;

    [Required(ErrorMessage = "Mật khẩu mới không được để trống.")]
    [MinLength(6, ErrorMessage = "Mật khẩu mới phải có tối thiểu 6 ký tự.")]
    public string NewPassword { get; set; } = string.Empty;

    [Required(ErrorMessage = "Xác nhận mật khẩu mới không được để trống.")]
    [Compare("NewPassword", ErrorMessage = "Mật khẩu xác nhận không trùng khớp.")]
    public string ConfirmPassword { get; set; } = string.Empty;
}
