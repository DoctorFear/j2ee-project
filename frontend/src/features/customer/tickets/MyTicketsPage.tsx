// Trang danh sách vé của khách hàng
import { useState, type ReactNode } from "react";
import { Ticket as TicketIcon } from "lucide-react";
import TicketCard from "@/features/customer/tickets/components/TicketCard";
import TicketTabs from "@/features/customer/tickets/components/TicketTabs";
import TicketDetailModal from "@/features/customer/tickets/components/TicketDetailModal";
import TicketQrModal from "@/features/customer/tickets/components/TicketQrModal";
import { tickets } from "@/features/customer/demo/demoTickets";
import { trips } from "@/features/customer/demo/demoTrips";
import type { Ticket } from "@/features/customer/types/ticket";
import "@/features/customer/tickets/tickets.css";

// Ghép danh sách vé và các hộp thoại chi tiết, QR
export default function MyTicketsPage({ header }: { header: ReactNode }) {
  const [tab, setTab] = useState<"PAID" | "COMPLETED">("PAID");
  const [modal, setModal] = useState<{ ticket: Ticket; qr: boolean } | null>(null);
  return (
    <div className="tickets-page">
      {header}
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
