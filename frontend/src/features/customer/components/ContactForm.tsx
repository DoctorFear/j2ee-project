// Biểu mẫu thông tin liên hệ người đặt vé
import { UserRound } from "lucide-react";
import type { Contact } from "../../../types/contact";
// Cập nhật họ tên, số điện thoại và email người đặt
export default function ContactForm({
  contact,
  onChange,
}: {
  contact: Contact;
  onChange: (value: Contact) => void;
}) {
  return (
    <div className="section-card">
      <div className="section-title">
        <UserRound size={18} />
        Thông tin khách hàng
      </div>
      {(["name", "phone", "email"] as const).map((key, i) => (
        <div className="form-group" key={key}>
          <label htmlFor={`contact-${key}`}>
            {["Họ và tên", "Số điện thoại", "Email"][i]} <span>*</span>
          </label>
          <input
            id={`contact-${key}`}
            className="form-control"
            autoComplete={["name", "tel", "email"][i]}
            type={["text", "tel", "email"][i]}
            required
            placeholder={
              [
                "Ví dụ: Nguyễn Văn A",
                "Ví dụ: 0912345678",
                "nhanve@example.com",
              ][i]
            }
            value={contact[key]}
            onChange={(e) => onChange({ ...contact, [key]: e.target.value })}
          />
        </div>
      ))}
    </div>
  );
}
