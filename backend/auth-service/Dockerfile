# syntax=docker/dockerfile:1
FROM mcr.microsoft.com/dotnet/sdk:10.0 AS build
WORKDIR /src
COPY auth-service/src/AuthService.Api/AuthService.Api.csproj ./
RUN dotnet restore AuthService.Api.csproj
COPY auth-service/src/AuthService.Api ./
RUN dotnet publish AuthService.Api.csproj --configuration Release --no-restore --output /app/publish

FROM mcr.microsoft.com/dotnet/aspnet:10.0
WORKDIR /app
COPY --from=build /app/publish ./
USER $APP_UID
ENTRYPOINT ["dotnet", "AuthService.Api.dll"]
