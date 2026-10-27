import { AtSign, Hash, Mail, Phone, ShieldCheck, UserRound } from "lucide-react";
import type { UserProfileResponseDto } from "@/features/auth";
import { Badge } from "@/components/ui/badge";
import styles from "../AdminAccount.module.css";

export function AccountDetails({ profile }: { profile: UserProfileResponseDto }) {
  const name = profile.fullName.trim() || profile.username;
  const role = profile.role === "Admin" ? "Quản trị viên (Admin)" : profile.role;
  const fields = [
    { label: "Họ và tên", value: profile.fullName.trim() || "Chưa cập nhật", icon: UserRound },
    { label: "Tên đăng nhập", value: profile.username, icon: AtSign },
    { label: "Email", value: profile.email.trim() || "Chưa cập nhật", icon: Mail },
    { label: "Số điện thoại", value: profile.phoneNumber?.trim() || "Chưa cập nhật", icon: Phone },
    { label: "Vai trò", value: role, icon: ShieldCheck },
    { label: "Mã tài khoản", value: String(profile.id), icon: Hash },
  ];

  return (
    <div className={styles.content}>
      <div className={`admin-panel ${styles.identity}`}>
        <span className={styles.avatar} aria-hidden="true">
          <UserRound size={30} />
        </span>
        <div className={styles.identityText}>
          <h2>{name}</h2>
          <p>@{profile.username}</p>
          <Badge variant="secondary">
            <ShieldCheck aria-hidden="true" />
            {role}
          </Badge>
        </div>
      </div>
      <section className="admin-panel" aria-labelledby="account-details-heading">
        <div className="admin-panel-heading">
          <h2 id="account-details-heading">Thông tin cá nhân</h2>
        </div>
        <dl className={styles.details}>
          {fields.map(({ label, value, icon: Icon }) => (
            <div className={styles.field} key={label}>
              <dt>
                <Icon size={16} aria-hidden="true" />
                {label}
              </dt>
              <dd>{value}</dd>
            </div>
          ))}
        </dl>
      </section>
    </div>
  );
}
