// Trang danh sách vé của khách hàng
import { useCallback, useEffect, useState } from "react";
import { Ticket as TicketIcon } from "lucide-react";
import type { ReactNode } from "react";
import { Link } from "react-router-dom";
import PageHero from "@/features/customer/components/PageHero";
import TicketCard from "./components/TicketCard";
import TicketTabs from "./components/TicketTabs";
import TicketDetailModal from "./components/TicketDetailModal";
import TicketQrModal from "./components/TicketQrModal";
import { bookingApi, type CustomerTicket } from "@/features/customer/api/bookingApi";
import type { Ticket } from "@/features/customer/types/ticket";
import "@/features/customer/tickets/tickets.css";

// Ghép danh sách vé và các hộp thoại chi tiết, QR
export default function MyTicketsPage({ header }: { header: ReactNode }) {
  const [tickets, setTickets] = useState<CustomerTicket[]>([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const load = useCallback(
    () =>
      bookingApi
        .tickets()
        .then((values) => {
          setTickets(values);
          setError("");
        })
        .catch((error) => setError(error.message))
        .finally(() => setLoading(false)),
      [],
  );
  useEffect(() => {
    load();
    const timer = setInterval(load, 15000);
    return () => clearInterval(timer);
  }, [load]);
  const [tab, setTab] = useState<"PAID" | "COMPLETED">("PAID");
  const [modal, setModal] = useState<{ ticket: Ticket; qr: boolean } | null>(
    null,
  );
  const currentTicket = tickets.find(
    (ticket) => ticket.code === modal?.ticket.code,
  );
  const visibleTickets = tickets.filter((ticket) =>
    tab === "PAID" ? ticket.status === "PAID" : ticket.status !== "PAID",
  );
  const bookingGroups = Array.from(
    visibleTickets
      .reduce((groups, ticket) => {
        const key = ticket.bookingId || ticket.code;
        groups.set(key, [...(groups.get(key) ?? []), ticket]);
          return groups;}, new Map<string, CustomerTicket[]>)
      .entries(),
  );
  return (
    <div className="tickets-page">
      {header}
      <PageHero
        icon={<TicketIcon size={26} />}
        title="Danh sách vé của tôi"
        subtitle="Xem, quản lý và hiển thị mã QR lên xe cho các chuyến đã đặt."
      >
        <span className="hero-chip">{tickets.filter((t) => t.status === "PAID").length} vé sắp đi</span>
      </PageHero>
      <div className="ticket-history-wrapper">
        <TicketTabs
          tab={tab}
          onChange={setTab}
          upcoming={tickets.filter((t) => t.status === "PAID").length}
          history={tickets.filter((t) => t.status !== "PAID").length}
        />
        {loading && <p role="status">Đang tải vé...</p>}
        {error && <p role="alert">{error}</p>}
        {!loading && !error && bookingGroups.length === 0 && (
          <div className="ticket-empty">
            <TicketIcon size={42} />
            <strong>{tab === "PAID" ? "Bạn chưa có vé sắp đi" : "Chưa có vé trong lịch sử"}</strong>
            <p>Tìm chuyến xe phù hợp và đặt vé chỉ trong vài bước.</p>
            <Link to="/" className="ticket-empty-btn">Tìm chuyến xe</Link>
          </div>
        )}
        <div className="ticket-list">
          {bookingGroups.map(([bookingId, bookingTickets]) => (
              <TicketCard
                key={bookingId}
                bookingId={bookingId}
                tickets={bookingTickets}
                onDetail={(ticket) => setModal({ ticket, qr: false })}
                onQr={(ticket) => setModal({ ticket, qr: true })}
              />
          ))}
        </div>
      </div>
      {modal &&
        currentTicket &&
        (modal.qr && currentTicket.status === "PAID" ? (
          <TicketQrModal
            ticket={currentTicket}
            onClose={() => {
              setModal(null);
              load();
            }}
          />
        ) : (
          <TicketDetailModal
            ticket={currentTicket}
            trip={currentTicket.trip}
            onClose={() => {
              setModal(null);
              load();
            }}
          />
        ))}
    </div>
  );
}
