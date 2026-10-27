export class ApiError extends Error {
  constructor(
    message: string,
    public status?: number,
    public field?: string
  ) {
    super(message);
    this.name = "ApiError";
  }
}
