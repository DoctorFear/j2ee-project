export class ApiError extends Error {
  constructor(
    message: string,
    public status?: number,
    public field?: string,
    public errorCode?: string,
    public errors?: unknown
  ) {
    super(message);
    this.name = "ApiError";
  }
}
