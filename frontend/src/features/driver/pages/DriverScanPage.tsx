// Màn hình 3: Quét mã QR soát vé & Nhập vé tay (/driver/trips/:code/scan)
import React, { useEffect, useState, useRef } from "react";
import { useParams, Link, useNavigate } from "react-router-dom";
import {
  ArrowLeft,
  Zap,
  Volume2,
  VolumeX,
  Keyboard,
  CheckCircle2,
  XCircle,
  QrCode,
  ShieldCheck,
  RefreshCw,
  X,
  Phone,
  Camera,
  Upload,
} from "lucide-react";
import jsQR from "jsqr";
import { driverApi } from "../api/driverApi";
import type { CheckInResult, DriverPassenger, DriverTripSummary } from "../types/driver";

export default function DriverScanPage() {
  const { code: tripCode } = useParams<{ code: string }>();
  const navigate = useNavigate();

  const [summary, setSummary] = useState<DriverTripSummary | null>(null);
  const [flashOn, setFlashOn] = useState<boolean>(false);
  const [soundOn, setSoundOn] = useState<boolean>(true);

  // Quản lý Camera stream
  const videoRef = useRef<HTMLVideoElement | null>(null);
  const streamRef = useRef<MediaStream | null>(null);
  const [cameraActive, setCameraActive] = useState<boolean>(false);
  const [cameraError, setCameraError] = useState<string | null>(null);
  const isScanningRef = useRef<boolean>(false);
  const fileInputRef = useRef<HTMLInputElement | null>(null);

  // Modal nhập tay (Mã vé hoặc 4 số cuối SĐT)
  const [showManualModal, setShowManualModal] = useState<boolean>(false);
  const [manualInput, setManualInput] = useState<string>("");
  const [manualSubmitting, setManualSubmitting] = useState<boolean>(false);

  // Kết quả soát vé (Bottom Sheet popup)
  const [scanResult, setScanResult] = useState<CheckInResult | null>(null);
  const [scanError, setScanError] = useState<string | null>(null);

  const loadSummary = async () => {
    if (!tripCode) return;
    try {
      const data = await driverApi.getSummary(tripCode);
      setSummary(data);
    } catch (err) {
      console.error("Lỗi khi tải tóm tắt chuyến:", err);
    }
  };

  useEffect(() => {
    loadSummary();
  }, [tripCode]);

  // Khởi động Camera thật từ Webcam / Camera thiết bị
  const startCamera = async () => {
    setCameraError(null);
    try {
      if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
        throw new Error("Trình duyệt không hỗ trợ API Camera.");
      }
      const stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: "environment", width: { ideal: 1280 }, height: { ideal: 720 } },
      });
      streamRef.current = stream;
      if (videoRef.current) {
        videoRef.current.srcObject = stream;
        videoRef.current.muted = true;
        await videoRef.current.play();
        setCameraActive(true);
      }
    } catch (err: any) {
      console.warn("Không thể bật camera:", err);
      if (err.name === "NotAllowedError" || err.name === "PermissionDeniedError") {
        setCameraError("Bạn chưa cấp quyền truy cập Camera cho trình duyệt.");
      } else {
        setCameraError("Không thể mở camera (" + (err.message || "Thiết bị bận") + ").");
      }
    }
  };

  const stopCamera = () => {
    if (streamRef.current) {
      streamRef.current.getTracks().forEach((t) => t.stop());
      streamRef.current = null;
    }
    setCameraActive(false);
  };

  useEffect(() => {
    startCamera();
    return () => {
      stopCamera();
    };
  }, []);

  // Vòng lặp nhận diện mã QR thời gian thực bằng thư viện jsQR thuần (hoạt động 100% trên mọi trình duyệt)
  useEffect(() => {
    if (!cameraActive) return;

    const canvas = document.createElement("canvas");
    const ctx = canvas.getContext("2d", { willReadFrequently: true });
    let animationFrameId: number;
    let lastScanTime = 0;

    const scanFrame = async (timestamp: number) => {
      // Quét mỗi 100ms để tối ưu hiệu năng CPU và giữ UI luôn mượt mà 60fps
      if (timestamp - lastScanTime >= 100) {
        lastScanTime = timestamp;

        if (videoRef.current && videoRef.current.readyState >= 2 && !isScanningRef.current) {
          const video = videoRef.current;
          if (video.videoWidth > 0 && video.videoHeight > 0) {
            // Giới hạn chiều rộng xử lý tối đa 800px để jsQR phân tích tức thì
            const maxW = 800;
            const scale = video.videoWidth > maxW ? maxW / video.videoWidth : 1;
            canvas.width = Math.round(video.videoWidth * scale);
            canvas.height = Math.round(video.videoHeight * scale);

            if (ctx) {
              ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
              const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
              const code = jsQR(imageData.data, imageData.width, imageData.height, {
                inversionAttempts: "attemptBoth",
              });

              if (code && code.data && code.data.trim()) {
                const raw = code.data.trim();
                isScanningRef.current = true;
                await executeCheckIn({ qrToken: raw });
                // Tạm dừng 2.5 giây sau khi quét thành công để tránh quét liên tục
                setTimeout(() => {
                  isScanningRef.current = false;
                }, 2500);
              }
            }
          }
        }
      }
      animationFrameId = requestAnimationFrame(scanFrame);
    };

    animationFrameId = requestAnimationFrame(scanFrame);

    return () => {
      cancelAnimationFrame(animationFrameId);
    };
  }, [cameraActive, tripCode]);

  // Âm thanh bíp khi soát vé thành công
  const playBeep = () => {
    if (!soundOn) return;
    try {
      const ctx = new (window.AudioContext || (window as any).webkitAudioContext)();
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();
      osc.type = "sine";
      osc.frequency.setValueAtTime(880, ctx.currentTime); // Note A5
      gain.gain.setValueAtTime(0.2, ctx.currentTime);
      osc.connect(gain);
      gain.connect(ctx.destination);
      osc.start();
      osc.stop(ctx.currentTime + 0.15);
    } catch (e) {
      // AudioContext có thể bị chặn nếu chưa có user gesture
    }
  };

  // Xử lý Check-in chung (thông minh: thử bằng qrToken, nếu không tìm thấy thì thử tiếp bằng ticketCode)
  const executeCheckIn = async (param: { ticketCode?: string; qrToken?: string; last4Phone?: string }) => {
    if (!tripCode) return;
    setScanError(null);
    setScanResult(null);

    try {
      let res: CheckInResult;
      try {
        res = await driverApi.checkIn(tripCode, param);
      } catch (err: any) {
        if (param.qrToken) {
          // Thử lại với ticketCode nếu mã quét được là mã vé
          res = await driverApi.checkIn(tripCode, { ticketCode: param.qrToken });
        } else {
          throw err;
        }
      }
      playBeep();
      setScanResult(res);
      await loadSummary();
    } catch (err: any) {
      const msg = err.response?.data?.message || err.message || "Vé không hợp lệ hoặc đã được sử dụng!";
      setScanError(msg);
    }
  };

  // Giả lập quét thành công khi người dùng bấm nút test nhanh
  const handleSimulatedScan = () => {
    if (!summary?.passengers || summary.passengers.length === 0) {
      setScanError("Chuyến xe này chưa có vé nào để soát thử!");
      return;
    }

    const pending = summary.passengers.find((p) => !p.boarded);
    if (!pending) {
      setScanError("Tất cả hành khách của chuyến này đều đã được soát vé!");
      return;
    }

    executeCheckIn({ ticketCode: pending.ticketCode });
  };

  // Thuật toán nhận diện mã QR thông minh đa lớp từ ảnh tĩnh
  const decodeQrFromImage = (img: HTMLImageElement): string | null => {
    const canvas = document.createElement("canvas");
    const ctx = canvas.getContext("2d", { willReadFrequently: true });
    if (!ctx) return null;

    // Lớp 1: Thử quét với kích thước ảnh gốc
    canvas.width = img.width;
    canvas.height = img.height;
    ctx.drawImage(img, 0, 0);
    let imgData = ctx.getImageData(0, 0, canvas.width, canvas.height);
    let code = jsQR(imgData.data, imgData.width, imgData.height, {
      inversionAttempts: "attemptBoth",
    });
    if (code && code.data && code.data.trim()) return code.data.trim();

    // Lớp 2: Nếu ảnh độ phân giải cao (> 1000px), thu nhỏ lại để jsQR nhận diện chuẩn xác hơn
    const maxDim = Math.max(img.width, img.height);
    if (maxDim > 1000) {
      const scale = 1000 / maxDim;
      canvas.width = Math.round(img.width * scale);
      canvas.height = Math.round(img.height * scale);
      ctx.drawImage(img, 0, 0, canvas.width, canvas.height);
      imgData = ctx.getImageData(0, 0, canvas.width, canvas.height);
      code = jsQR(imgData.data, canvas.width, canvas.height, {
        inversionAttempts: "attemptBoth",
      });
      if (code && code.data && code.data.trim()) return code.data.trim();
    }

    // Lớp 3: Crop vùng trung tâm 70% (nơi chứa mã QR của ảnh chụp màn hình vé)
    const cropW = Math.round(img.width * 0.7);
    const cropH = Math.round(img.height * 0.7);
    const startX = Math.round((img.width - cropW) / 2);
    const startY = Math.round((img.height - cropH) / 2);
    canvas.width = cropW;
    canvas.height = cropH;
    ctx.drawImage(img, startX, startY, cropW, cropH, 0, 0, cropW, cropH);
    imgData = ctx.getImageData(0, 0, cropW, cropH);
    code = jsQR(imgData.data, cropW, cropH, {
      inversionAttempts: "attemptBoth",
    });
    if (code && code.data && code.data.trim()) return code.data.trim();

    return null;
  };

  // Quét mã QR từ file ảnh người dùng tải lên
  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setScanError(null);
    const reader = new FileReader();
    reader.onload = (event) => {
      const img = new Image();
      img.onload = () => {
        const decoded = decodeQrFromImage(img);
        if (decoded) {
          executeCheckIn({ qrToken: decoded });
        } else {
          setScanError("Không tìm thấy mã QR trong bức ảnh này! Vui lòng chọn ảnh chụp rõ nét hơn.");
        }
      };
      img.onerror = () => {
        setScanError("Tệp ảnh không hợp lệ.");
      };
      img.src = event.target?.result as string;
    };
    reader.readAsDataURL(file);

    if (fileInputRef.current) fileInputRef.current.value = "";
  };

  // Xử lý gửi biểu mẫu nhập tay
  const handleManualSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const val = manualInput.trim();
    if (!val) return;

    setManualSubmitting(true);
    try {
      if (val.length === 4 && /^\d+$/.test(val)) {
        await executeCheckIn({ last4Phone: val });
      } else {
        await executeCheckIn({ ticketCode: val });
      }
      setShowManualModal(false);
      setManualInput("");
    } finally {
      setManualSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 text-white flex flex-col font-sans select-none overflow-hidden">
      {/* Top Bar */}
      <header className="p-4 flex items-center justify-between border-b border-slate-800 bg-slate-900/80 backdrop-blur-md z-20">
        <Link
          to={`/driver/trips/${encodeURIComponent(tripCode || "")}/passengers`}
          className="w-10 h-10 rounded-full bg-slate-800 hover:bg-slate-700 flex items-center justify-center text-slate-300 transition-colors text-decoration-none"
        >
          <ArrowLeft size={20} />
        </Link>

        <div className="text-center">
          <div className="text-xs text-orange-400 font-bold uppercase tracking-wider">Soát vé điện tử</div>
          <h2 className="text-sm font-extrabold text-white m-0">Chuyến {tripCode}</h2>
        </div>

        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={() => setFlashOn(!flashOn)}
            className={`w-10 h-10 rounded-full flex items-center justify-center transition-colors cursor-pointer ${
              flashOn ? "bg-amber-400 text-slate-950 font-bold" : "bg-slate-800 text-slate-300 hover:bg-slate-700"
            }`}
            title="Bật/Tắt Flash"
          >
            <Zap size={18} />
          </button>

          <button
            type="button"
            onClick={() => setSoundOn(!soundOn)}
            className={`w-10 h-10 rounded-full flex items-center justify-center transition-colors cursor-pointer ${
              soundOn ? "bg-slate-800 text-emerald-400" : "bg-slate-800 text-slate-500"
            }`}
            title="Âm thanh"
          >
            {soundOn ? <Volume2 size={18} /> : <VolumeX size={18} />}
          </button>
        </div>
      </header>

      {/* Camera Viewfinder Area */}
      <main className="flex-1 flex flex-col items-center justify-center p-6 relative">
        <div className="text-center mb-5 space-y-1">
          <p className="text-xs text-slate-200 font-semibold m-0">Hướng camera vào mã QR vé điện tử của hành khách</p>
          <p className="text-[11px] text-emerald-400 m-0">
            Hệ thống đang tự động nhận diện QR thời gian thực...
          </p>
        </div>

        {/* Khung ngắm Camera kèm video stream thật */}
        <div className="relative w-72 h-72 sm:w-80 sm:h-80 rounded-3xl border-2 border-emerald-500/60 bg-emerald-950/40 overflow-hidden flex items-center justify-center shadow-[0_0_50px_rgba(16,185,129,0.2)]">
          {/* Video stream từ webcam thật */}
          <video
            ref={videoRef}
            playsInline
            autoPlay
            muted
            className={`w-full h-full object-cover transition-opacity duration-300 ${
              cameraActive ? "opacity-100" : "opacity-0"
            }`}
          />

          {/* Placeholder khi camera chưa mở hoặc lỗi quyền */}
          {!cameraActive && (
            <div className="absolute inset-0 flex flex-col items-center justify-center p-4 text-center space-y-2 bg-slate-900/90">
              <Camera size={40} className="text-slate-500 animate-pulse" />
              <p className="text-xs text-slate-400">
                {cameraError || "Đang kết nối Camera..."}
              </p>
              {cameraError && (
                <button
                  type="button"
                  onClick={startCamera}
                  className="px-3 py-1.5 rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold transition-colors cursor-pointer"
                >
                  Thử lại bật Camera
                </button>
              )}
            </div>
          )}

          {/* 4 Góc bo bảo vệ của khung ngắm */}
          <div className="absolute top-3 left-3 w-6 h-6 border-t-4 border-l-4 border-emerald-400 rounded-tl-lg pointer-events-none"></div>
          <div className="absolute top-3 right-3 w-6 h-6 border-t-4 border-r-4 border-emerald-400 rounded-tr-lg pointer-events-none"></div>
          <div className="absolute bottom-3 left-3 w-6 h-6 border-b-4 border-l-4 border-emerald-400 rounded-bl-lg pointer-events-none"></div>
          <div className="absolute bottom-3 right-3 w-6 h-6 border-b-4 border-r-4 border-emerald-400 rounded-br-lg pointer-events-none"></div>

          {/* Tia laser quét chạy dọc */}
          {cameraActive && (
            <div className="absolute left-4 right-4 h-0.5 bg-gradient-to-r from-transparent via-emerald-400 to-transparent shadow-[0_0_12px_#34d399] animate-pulse pointer-events-none"></div>
          )}
        </div>

        {/* Hàng nút chức năng bổ trợ: Tải ảnh QR & Giả lập duyệt thử */}
        <div className="flex items-center gap-2.5 mt-5">
          <input
            type="file"
            ref={fileInputRef}
            accept="image/*"
            onChange={handleFileUpload}
            className="hidden"
          />

          <button
            type="button"
            onClick={() => fileInputRef.current?.click()}
            className="py-2 px-3.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-bold flex items-center gap-1.5 transition-colors cursor-pointer border border-slate-700 shadow-sm"
          >
            <Upload size={14} className="text-emerald-400" /> Tải ảnh QR lên
          </button>

          <button
            type="button"
            onClick={handleSimulatedScan}
            className="py-2 px-3.5 rounded-xl bg-slate-800/80 hover:bg-slate-700 text-slate-300 text-xs font-semibold flex items-center gap-1.5 transition-colors cursor-pointer border border-slate-700/60"
            title="Duyệt thử 1 vé chưa lên xe để test nhanh"
          >
            <Zap size={14} className="text-amber-400" /> Giả lập duyệt 1 vé
          </button>
        </div>

        {/* Thông báo lỗi quét nếu có */}
        {scanError && (
          <div className="mt-4 p-3 px-4 rounded-xl bg-rose-950/80 border border-rose-600/60 text-rose-200 text-xs font-semibold flex items-center gap-2 animate-in fade-in max-w-sm text-center">
            <XCircle size={18} className="text-rose-400 shrink-0" />
            <span>{scanError}</span>
          </div>
        )}
      </main>

      {/* Footer Controls */}
      <footer className="p-5 border-t border-slate-800 bg-slate-900/90 space-y-3 z-20">
        <div className="flex items-center justify-between">
          <div>
            <div className="text-[11px] font-bold text-slate-400 uppercase">Tiến độ lên xe</div>
            <div className="text-lg font-black text-white">
              {summary?.totalBoarded || 0} / {summary?.totalPassengers || 0}{" "}
              <span className="text-xs text-slate-400 font-bold">khách</span>
            </div>
          </div>

          <button
            type="button"
            onClick={() => setShowManualModal(true)}
            className="py-2.5 px-4 rounded-xl bg-orange-600 hover:bg-orange-700 text-white font-bold text-xs flex items-center gap-2 transition-colors cursor-pointer shadow-xs"
          >
            <Keyboard size={16} /> Nhập vé tay / SĐT
          </button>
        </div>
      </footer>

      {/* MODAL 1: Nhập vé tay khi QR mờ hoặc khách hết pin */}
      {showManualModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-xs p-4 animate-in fade-in">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-md p-5 text-white shadow-2xl space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-base font-bold m-0 flex items-center gap-2">
                <Keyboard size={18} className="text-orange-500" /> Nhập vé thủ công
              </h3>
              <button
                type="button"
                onClick={() => setShowManualModal(false)}
                className="w-8 h-8 rounded-full bg-slate-800 text-slate-400 hover:text-white flex items-center justify-center"
              >
                <X size={16} />
              </button>
            </div>

            <p className="text-xs text-slate-400 m-0">
              Nhập <strong>mã vé đầy đủ</strong> (vd: SEED-T01-A01) hoặc <strong>4 số cuối số điện thoại</strong> của khách hàng (vd: 5601):
            </p>

            <form onSubmit={handleManualSubmit} className="space-y-4">
              <input
                type="text"
                autoFocus
                placeholder="Nhập mã vé hoặc 4 số cuối SĐT..."
                value={manualInput}
                onChange={(e) => setManualInput(e.target.value)}
                className="w-full px-4 py-3 rounded-xl bg-slate-800 border border-slate-700 text-white text-sm focus:outline-none focus:border-orange-500 transition-colors"
              />

              <div className="flex items-center justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setShowManualModal(false)}
                  className="px-4 py-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 font-semibold text-xs"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  disabled={manualSubmitting || !manualInput.trim()}
                  className="px-5 py-2.5 rounded-xl bg-orange-600 hover:bg-orange-700 disabled:opacity-50 text-white font-bold text-xs"
                >
                  {manualSubmitting ? "Đang soát..." : "Xác nhận soát vé"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* POPUP BOTTOM SHEET: Kết quả sau khi soát vé thành công */}
      {scanResult && (
        <div className="fixed inset-0 z-50 flex items-end justify-center bg-black/60 backdrop-blur-xs p-4 animate-in fade-in">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl w-full max-w-md p-6 text-white shadow-2xl space-y-4 animate-in slide-in-from-bottom-5">
            <div className="w-12 h-1 bg-slate-700 rounded-full mx-auto"></div>

            <div className="flex items-center gap-3">
              <div className="w-12 h-12 rounded-2xl bg-emerald-500/20 text-emerald-400 flex items-center justify-center shrink-0 border border-emerald-500/30">
                <CheckCircle2 size={28} />
              </div>
              <div>
                <span className="text-[11px] font-extrabold text-emerald-400 uppercase tracking-wider">Hợp lệ</span>
                <h3 className="text-lg font-black text-white m-0">Soát vé thành công!</h3>
              </div>
            </div>

            {/* Chi tiết vé vừa soát */}
            <div className="bg-slate-800/80 rounded-2xl p-4 border border-slate-700/60 space-y-2.5">
              <div className="flex justify-between items-center text-xs">
                <span className="text-slate-400">Mã vé:</span>
                <span className="font-extrabold text-orange-400">{scanResult.ticketCode}</span>
              </div>
              <div className="flex justify-between items-center text-xs">
                <span className="text-slate-400">Vị trí ghế:</span>
                <span className="font-black text-white text-base px-2 py-0.5 rounded-md bg-slate-700 border border-slate-600">
                  {scanResult.seatCode}
                </span>
              </div>
              <div className="flex justify-between items-center text-xs">
                <span className="text-slate-400">Hành khách:</span>
                <span className="font-bold text-slate-200">{scanResult.customerName}</span>
              </div>
              <div className="flex justify-between items-center text-xs">
                <span className="text-slate-400">Điểm đón:</span>
                <span className="font-semibold text-slate-300">{scanResult.pickupStationName}</span>
              </div>
            </div>

            <button
              type="button"
              onClick={() => setScanResult(null)}
              className="w-full py-3 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-sm transition-colors cursor-pointer shadow-md"
            >
              Tiếp tục soát vé khác
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
