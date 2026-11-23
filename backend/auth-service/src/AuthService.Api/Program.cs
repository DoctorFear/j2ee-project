using System;
using System.Collections.Generic;
using System.Text;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.AspNetCore.Builder;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using Microsoft.IdentityModel.Tokens;
using Microsoft.OpenApi;
using AuthService.Api.Data;
using AuthService.Api.Data.Seed;
using AuthService.Api.Repositories;
using AuthService.Api.Services;

var builder = WebApplication.CreateBuilder(args);

// 1. Cấu hình DbContext kết nối PostgreSQL (FutaBus_AuthDb)
var connectionString = builder.Configuration
    .GetConnectionString("DefaultConnection");

if (string.IsNullOrWhiteSpace(connectionString))
{
    throw new InvalidOperationException(
        "Không tìm thấy ConnectionStrings:DefaultConnection"
    );
}

builder.Services.AddDbContext<AuthDbContext>(options =>
    options.UseNpgsql(connectionString));

// 2. Đăng ký Dependency Injection (DI)
builder.Services.AddScoped<IPasswordHasher, BcryptPasswordHasher>();
builder.Services.AddScoped<ITokenService, TokenService>();
builder.Services.AddScoped<IEmailService, EmailService>();
builder.Services.AddScoped<IUserRepository, UserRepository>();
builder.Services.AddScoped<IAuthService, AuthenticationService>();
builder.Services.AddScoped<IUserService, UserService>();

builder.Services.AddControllers();

// 3. Cấu hình CORS cho Frontend và Gateway
builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowAll", policy =>
    {
        policy.WithOrigins("http://localhost:5173", "http://localhost:8080")
              .AllowAnyHeader()
              .AllowAnyMethod()
              .AllowCredentials();
    });
});

// 4. Cấu hình JWT Bearer Authentication
var jwtSettings = builder.Configuration.GetSection("JwtSettings");
var secretKey = Encoding.UTF8.GetBytes(jwtSettings["Secret"] ?? "FutaBusSuperSecretKeyWithAtLeast32BytesLengthForMicroservice!");

builder.Services.AddAuthentication(options =>
{
    options.DefaultAuthenticateScheme = JwtBearerDefaults.AuthenticationScheme;
    options.DefaultChallengeScheme = JwtBearerDefaults.AuthenticationScheme;
})
.AddJwtBearer(options =>
{
    options.RequireHttpsMetadata = false;
    options.SaveToken = true;
    options.TokenValidationParameters = new TokenValidationParameters
    {
        ValidateIssuerSigningKey = true,
        IssuerSigningKey = new SymmetricSecurityKey(secretKey),
        ValidateIssuer = true,
        ValidIssuer = jwtSettings["Issuer"] ?? "FutaBusAuthApi",
        ValidateAudience = true,
        ValidAudience = jwtSettings["Audience"] ?? "FutaBusClient",
        ValidateLifetime = true,
        ClockSkew = TimeSpan.Zero
    };
});

builder.Services.AddAuthorization();
builder.Services.AddEndpointsApiExplorer();

// 5. Cấu hình Swagger UI có nút nhập Bearer Token
builder.Services.AddSwaggerGen(c =>
{
    c.SwaggerDoc("v1", new OpenApiInfo { Title = "FutaBus Auth Service API", Version = "v1" });

    c.AddSecurityDefinition("Bearer", new OpenApiSecurityScheme
    {
        Name = "Authorization",
        Description = "Nhập token JWT (không cần gõ tiền tố Bearer)",
        In = ParameterLocation.Header,
        Type = SecuritySchemeType.Http,
        Scheme = "bearer",
        BearerFormat = "JWT"
    });

    c.AddSecurityRequirement(document => new OpenApiSecurityRequirement
    {
        {
            new OpenApiSecuritySchemeReference("Bearer", document),
            new List<string>()
        }
    });
});

var app = builder.Build();

// 5.1. Cấu hình ContextPath (tương tự server.servlet.context-path trong Spring Boot)
var contextPath = app.Configuration["Server:ContextPath"]
    ?? app.Configuration["server:servlet:context-path"]
    ?? app.Configuration["Server:Servlet:ContextPath"]
    ?? app.Configuration["PathBase"]
    ?? "/api";

if (!string.IsNullOrWhiteSpace(contextPath))
{
    var normalizedPath = "/" + contextPath.Trim('/');
    app.UsePathBase(normalizedPath);
}

// 6. Tự động Seed tài khoản Admin đầu tiên nếu chưa có
using (var scope = app.Services.CreateScope())
{
    var services = scope.ServiceProvider;
    var migrateOnStartup = app.Configuration.GetValue<bool>("Database:MigrateOnStartup");

    try
    {
        var context = services.GetRequiredService<AuthDbContext>();
        if (migrateOnStartup)
        {
            await context.Database.MigrateAsync();
        }

        var passwordHasher = services.GetRequiredService<IPasswordHasher>();
        await DatabaseSeeder.SeedAsync(context, passwordHasher);
    }
    catch (Exception ex) when (!migrateOnStartup)
    {
        var logger = services.GetRequiredService<ILogger<Program>>();
        logger.LogWarning("Chưa thể seed tài khoản Auth trong PostgreSQL: {Message}", ex.Message);
    }
}

if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseCors("AllowAll");
app.UseAuthentication();
app.UseAuthorization();

// Health check endpoint
app.MapGet("/health", () => Results.Ok(new { status = "UP", service = "auth-service" }));

app.MapControllers();

app.Run();
