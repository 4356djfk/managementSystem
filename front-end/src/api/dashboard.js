import { http } from './http'

export function getDashboardHome() {
  return http('/dashboard/home')
}
