import { enumLabels } from "../constants/enum-labels";
import { formatMoney } from "./format-money";

const moneyFields = new Set(["basePrice", "ticketPrice", "collectedAmount", "refundedAmount"]);
export function formatValue(field: string, value: unknown): string {
  if (value === null || value === undefined || value === "") return "—";
  if (typeof value === "boolean") return value ? "Có" : "Không";
  if (moneyFields.has(field) && typeof value === "number") return formatMoney(value);
  if ((field.endsWith("At") || field === "licenseExpiryOn") && typeof value === "string")
    return value.replace("T", " ").slice(0, 19);
  return enumLabels[String(value)] ?? String(value);
}
