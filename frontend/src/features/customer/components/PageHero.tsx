// Banner tiêu đề dùng chung cho các trang khách hàng
import type { ReactNode } from "react";
import "./page-hero.css";

export default function PageHero({
     icon,
     title,
     subtitle,
     children,
}: {
    icon?: ReactNode;
    title: string;
    subtitle?: string;
    children?: ReactNode;
}) {
    return (
        <section className="page-hero">
            <div className="page-hero-inner">
                <div className="page-hero-text">
                    {icon && <span className="page-hero-icon">{icon}</span>}
                    <div>
                        <h1>{title}</h1>
                        {subtitle && <p>{subtitle}</p>}
                    </div>
                </div>
                {children && <div className="page-hero-extra">{children}</div>}
            </div>
        </section>
    );
}
