using System.Security.Claims;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using AuthService.Api.Dtos.Common;
using AuthService.Api.Dtos.User;
using AuthService.Api.Services;

namespace AuthService.Api.Controllers;

[Authorize]
[ApiController]
[Route("api/[controller]")]
public class UserController : ControllerBase
{
    private readonly IUserService _userService;

    public UserController(IUserService userService)
    {
        _userService = userService;
    }

    private int GetCurrentUserId()
    {
        var claim = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
        return int.TryParse(claim, out int userId) ? userId : 0;
    }

    [HttpGet("profile")]
    public async Task<IActionResult> GetProfile()
    {
        var userId = GetCurrentUserId();
        if (userId <= 0)
        {
            return Unauthorized(ApiResponse<UserProfileResponseDto>.Fail("Không xác thực được người dùng."));
        }

        var result = await _userService.GetProfileAsync(userId);
        return result.Success ? Ok(result) : BadRequest(result);
    }

    [HttpPut("profile")]
    public async Task<IActionResult> UpdateProfile([FromBody] UpdateProfileRequestDto request)
    {
        if (!ModelState.IsValid)
        {
            return BadRequest(ModelState);
        }

        var userId = GetCurrentUserId();
        if (userId <= 0)
        {
            return Unauthorized(ApiResponse<UserProfileResponseDto>.Fail("Không xác thực được người dùng."));
        }

        var result = await _userService.UpdateProfileAsync(userId, request);
        return result.Success ? Ok(result) : BadRequest(result);
    }

    [HttpPost("change-password")]
    public async Task<IActionResult> ChangePassword([FromBody] ChangePasswordRequestDto request)
    {
        if (!ModelState.IsValid)
        {
            return BadRequest(ModelState);
        }

        var userId = GetCurrentUserId();
        if (userId <= 0)
        {
            return Unauthorized(ApiResponse<bool>.Fail("Không xác thực được người dùng."));
        }

        var result = await _userService.ChangePasswordAsync(userId, request);
        return result.Success ? Ok(result) : BadRequest(result);
    }
}
