import { createResourceApi } from "../shared/api/create-resource-api";

export const routeStopsApi = createResourceApi("route-stops", ({ routeId }) => (routeId ? { routeId } : undefined));
