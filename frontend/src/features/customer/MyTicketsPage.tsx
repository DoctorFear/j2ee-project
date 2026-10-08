// Trang danh sách vé của khách hàng
import { useState } from "react";
import { Ticket as TicketIcon } from "lucide-react";
import CustomerHeader from "../../layouts/CustomerHeader";
import TicketCard from "./components/TicketCard";
import TicketTabs from "./components/TicketTabs";
import TicketDetailModal from "./components/TicketDetailModal";
import TicketQrModal from "./components/TicketQrModal";
import { tickets, trips } from "../../data/customer";
import type { Ticket } from "../../types/ticket";
import "./tickets.css";

// Ghép danh sách vé và các hộp thoại chi tiết, QR
export default function MyTicketsPage() {
  const [tab, setTab] = useState<"PAID" | "COMPLETED">("PAID");
  const [modal, setModal] = useState<{ ticket: Ticket; qr: boolean } | null>(
    null,
  );
  return (
    <div className="tickets-page">
      <CustomerHeader />
      <div className="ticket-history-wrapper">
        <div className="page-title-row">
          <h1 className="page-title">
            <TicketIcon size={26} />
            Danh sách vé của tôi
          </h1>
        </div>
        <TicketTabs
          tab={tab}
          onChange={setTab}
          upcoming={tickets.filter((t) => t.status === "PAID").length}
          history={tickets.filter((t) => t.status === "COMPLETED").length}
        />
        <div className="ticket-list">
          {tickets
            .filter((t) => t.status === tab)
            .map((ticket) => (
              <TicketCard
                key={ticket.code}
                ticket={ticket}
                trip={trips.find((t) => t.code === ticket.tripCode)!}
                onDetail={() => setModal({ ticket, qr: false })}
                onQr={() => setModal({ ticket, qr: true })}
              />
            ))}
        </div>
      </div>
      {modal &&
        (modal.qr ? (
          <TicketQrModal ticket={modal.ticket} onClose={() => setModal(null)} />
        ) : (
          <TicketDetailModal
            ticket={modal.ticket}
            trip={trips.find((t) => t.code === modal.ticket.tripCode)!}
            onClose={() => setModal(null)}
          />
        ))}
    </div>
  );
}
