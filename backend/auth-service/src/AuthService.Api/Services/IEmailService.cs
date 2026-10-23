using System.Threading.Tasks;

namespace AuthService.Api.Services;

public interface IEmailService
{
    Task SendEmailAsync(string toEmail, string subject, string body);
}
