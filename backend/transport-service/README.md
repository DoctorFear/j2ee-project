# API tuyến và điểm dừng

Dùng URL `http://localhost:8081` khi gọi trực tiếp hoặc `http://localhost:8080` khi gọi qua Gateway.
Phản hồi thành công có nội dung dùng `ApiResponse` với các trường `success`, `data` và `timestamp`.
Khi có lỗi, Spring trả trực tiếp `ProblemDetail` với `Content-Type: application/problem+json` và
các trường `status`, `detail`, `errorCode`. Với lỗi kiểm tra dữ liệu đầu vào,
trường `errors` chứa danh sách thông báo cho từng trường có lỗi.

| Tài nguyên | Tạo | Danh sách | Chi tiết | Cập nhật | Xóa |
| ---------- | --- | --------- | -------- | -------- | --- |
| Tuyến (`Route`) | `POST /api/routes` | `GET /api/routes` | `GET /api/routes/{id}` | `PUT /api/routes/{id}` | `DELETE /api/routes/{id}` |
| Điểm dừng (`RouteStop`) | `POST /api/route-stops` | `GET /api/route-stops` | `GET /api/route-stops/{id}` | `PUT /api/route-stops/{id}` | `DELETE /api/route-stops/{id}` |

Khi tạo thành công, API trả **201** và header `Location`. Thao tác đọc và cập nhật
trả **200**. Thao tác xóa trả **204** với thân phản hồi rỗng. Danh sách trả toàn bộ
kết quả trong một lần, giống API trạm (`Station)). Các controller này không áp dụng
thêm bước xác thực.

## Tuyến (`Route`)

Thân yêu cầu khi tạo tuyến

```json
{
  "code": "HCM-DL",
  "versionNo": 1,
  "name": "Ho Chi Minh - Da Lat",
  "basePrice": 250000,
  "effectiveFromAt": "2026-10-21T08:00:00",
  "effectiveToAt": null
}
```

Các trường đều bắt buộc ngoại trừ `effectiveToAt`. `code` và `name` phải có nội dung
và dài tối đa 255 ký tự. `versionNo` phải lớn hơn 0. `basePrice` là số nguyên
không âm, tối đa 15 chữ số. Giá trị ngày giờ dùng định dạng giờ địa phương,
không kèm múi giờ. Nếu gửi `effectiveToAt`, giá trị này phải sau `effectiveFromAt`.

Tuyến mới có `status: "INACTIVE"`. Yêu cầu PUT dùng các trường như khi tạo tuyến
và thêm `status: "ACTIVE"` hoặc `"INACTIVE"`. Bỏ qua `effectiveToAt`
hoặc gửi giá trị `null` để xóa thời điểm kết thúc hiệu lực đã lưu. Phản hồi
chứa các trường lúc tạo cùng với `id` và `status`.

Mỗi cặp `(code, versionNo)` là duy nhất. Tạo hoặc cập nhật trùng cặp giá trị
này trả **409**. Cần xóa các điểm dừng trước khi xóa tuyến. Nếu tuyến vẫn còn
điểm dừng, API trả **409**. ID tài nguyên không tồn tại trả **404**. Thân yêu cầu
không hợp lệ trả **400**.

## Điểm dừng (`RouteStop`)

POST và PUT yêu cầu tất cả các trường dưới đây, kể cả hai trường kiểu boolean.

```json
{
  "routeId": 1,
  "stationId": 1,
  "stopOrder": 0,
  "distanceKm": 0,
  "offsetMinutes": 0,
  "allowPickup": true,
  "allowDropOff": false
}
```

`routeId` và `stationId` phải là ID dương của bản ghi đang tồn tại. Nếu tuyến
hoặc trạm được tham chiếu không tồn tại, API trả **404**. `stopOrder` và
`offsetMinutes` là số nguyên không âm. `distanceKm` là số không âm với tối đa
8 chữ số ở phần nguyên và 2 chữ số ở phần thập phân. Khoảng cách và số phút di chuyển
được tính lũy kế từ đầu tuyến. API không kiểm tra các giá trị này có tăng dần
giữa các điểm dừng hay không và không tự đánh lại thứ tự điểm dừng.

Phản hồi trả các trường trên cùng với `id` dưới dạng JSON từ DTO. PUT có thể thay đổi tuyến hoặc trạm
của điểm dừng. Cặp `(routeId, stopOrder)` phải duy nhất. Nếu bị trùng, API trả
**409**, kể cả khi chuyển điểm dừng sang tuyến khác. Một trạm có thể xuất hiện
ở nhiều điểm dừng. Thân yêu cầu không hợp lệ trả **400**.

`GET /api/route-stops?routeId=1` liệt kê các điểm dừng của tuyến theo thứ tự
`stopOrder` tăng dần. Tuyến không tồn tại trả **404**. Tuyến chưa có điểm dừng
trả mảng rỗng. Khi không lọc theo tuyến, kết quả được sắp xếp theo `routeId`,
sau đó theo `stopOrder`. ID điểm dừng không tồn tại trả **404**. Xóa điểm dừng
chỉ xóa bản ghi điểm dừng. Nếu một trạm còn được điểm dừng tham chiếu, yêu cầu
xóa trạm trả **409**.

Bộ sưu tập Postman trong `../postman` có yêu cầu mẫu cho cả hai tài nguyên,
gồm gọi trực tiếp và gọi qua Gateway. Hãy tạo trạm và tuyến trước khi tạo
điểm dừng. Khi xóa, hãy xóa điểm dừng trước khi xóa bản ghi cha.

Chạy `./mvnw test` để kiểm thử API với H2. Trên Windows, chạy `.\mvnw.cmd test`.

## Xe và tài xế (`Vehicle` và `Driver`)

API xe và tài xế dùng cùng cấu trúc phản hồi và mã HTTP như API trạm. Hai API
chỉ hỗ trợ tạo, xem danh sách, xem chi tiết và xóa. PUT và PATCH không được hỗ trợ.
Có thể gọi cả hai API qua Gateway.

| Tài nguyên | Tạo | Danh sách | Chi tiết | Xóa |
| ---------- | --- | --------- | -------- | --- |
| Xe (`Vehicle`) | `POST /api/vehicles` | `GET /api/vehicles` | `GET /api/vehicles/{id}` | `DELETE /api/vehicles/{id}` |
| Tài xế (`Driver`) | `POST /api/drivers` | `GET /api/drivers` | `GET /api/drivers/{id}` | `DELETE /api/drivers/{id}` |

Tất cả các trường trong thân yêu cầu tạo xe đều bắt buộc.

```json
{
  "licensePlate": "51B-12345",
  "type": "SEAT",
  "capacity": 45
}
```

`licensePlate` phải có nội dung và dài tối đa 255 ký tự. `type` nhận một trong
ba giá trị `SEAT`, `BERTH` và `LIMOUSINE`. `capacity` là số nguyên dương.

Thân yêu cầu khi tạo tài xế

```json
{
  "fullName": "Nguyen Van A",
  "phone": "0901234567",
  "licenseNumber": "GPLX123",
  "licenseClass": "D",
  "licenseExpiryOn": "2030-01-01T00:00:00"
}
```

`fullName` và `phone` là trường bắt buộc và phải có nội dung. Mọi trường kiểu
chuỗi dài tối đa 255 ký tự. Các trường về giấy phép lái xe là tùy chọn.
`licenseExpiryOn` dùng ngày giờ địa phương, không kèm múi giờ.

Xe và tài xế mới có `status: "INACTIVE"`. Phản hồi chứa các trường lúc tạo
cùng với `id` và `status`. POST trả **201** và header `Location`. GET trả
**200**. DELETE trả **204** với thân phản hồi rỗng. Danh sách trả toàn bộ kết
quả trong một lần. Dữ liệu đầu vào không hợp lệ trả **400**. Xem hoặc xóa một
ID không tồn tại trả **404**.

## Chuyến xe và phân công tài xế (`Trip` và `TripDriver`)

Hai API dùng cùng cấu trúc phản hồi như API trạm. POST trả **201** và header
`Location`. GET trả **200**. DELETE trả **204** với thân phản hồi rỗng. API
chỉ hỗ trợ tạo, xem danh sách, xem chi tiết và xóa. PUT và PATCH trả **405**.

| Tài nguyên | Tạo / Danh sách | Chi tiết / Xóa |
| ---------- | --------------- | -------------- |
| Chuyến xe (`Trip`) | `POST /api/trips`, `GET /api/trips` | `GET /api/trips/{id}`, `DELETE /api/trips/{id}` |
| Phân công tài xế (`TripDriver`) | `POST /api/trip-drivers`, `GET /api/trip-drivers` | `GET /api/trip-drivers/{id}`, `DELETE /api/trip-drivers/{id}` |

Có thể gọi cả hai API qua Gateway. Danh sách trả toàn bộ kết quả trong một lần.

Tất cả các trường trong thân yêu cầu tạo chuyến xe đều bắt buộc.

```json
{
  "code": "HCM-DL-001",
  "routeId": 1,
  "vehicleId": 1,
  "departureAt": "2026-10-23T08:00:00",
  "arrivalAt": "2026-10-23T14:00:00",
  "ticketPrice": 250000
}
```

`code` phải có nội dung và dài tối đa 255 ký tự. ID tham chiếu phải là số dương
của bản ghi đang tồn tại. `arrivalAt` phải sau `departureAt`. Cả hai trường dùng
ngày giờ địa phương, không kèm múi giờ. `ticketPrice` là số nguyên không âm,
tối đa 15 chữ số. Chuyến xe mới có `status: "DRAFT"` và `saleStatus: "NOT_OPEN"`.
Hệ thống lưu sức chứa của xe được chọn vào `capacitySnapshot` tại thời điểm
tạo chuyến. Phản hồi cũng có `id` và các mốc thời gian dự kiến, thực tế. Các mốc
này có giá trị ban đầu là `null`.

Tất cả các trường trong thân yêu cầu phân công tài xế đều bắt buộc.

```json
{
  "tripId": 1,
  "driverId": 1,
  "role": "PRIMARY"
}
```

`tripId` và `driverId` phải là ID dương của bản ghi đang tồn tại. `role` nhận
`PRIMARY` hoặc `RELIEF`. Phản hồi chứa các trường này cùng với `id`. Phân
công lại cùng một tài xế cho cùng chuyến xe trả **409** dù vai trò khác nhau.

Dữ liệu đầu vào không hợp lệ trả **400**. Tài nguyên hoặc bản ghi tham chiếu
không tồn tại trả **404**. Cần xóa các phân công tài xế trước khi xóa chuyến xe.
Nếu chuyến xe vẫn còn phân công, API trả **409**. Xóa phân công giữ lại chuyến xe
và tài xế. Xóa chuyến xe giữ lại tuyến và xe.
