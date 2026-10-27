export interface EntityListState {
  q: string;
  status: string;
  sortKey: string;
  desc: boolean;
  page: number;
  pageSize: number;
  routeId?: number;
}
