import { defineConfig } from 'vite';
import { resolve } from 'path';

export default defineConfig({
  root: '.',
  base: '/',
  publicDir: 'public',
  build: {
    outDir: 'dist',
    emptyOutDir: true,
    sourcemap: true,
    rollupOptions: {
      input: {
        main: resolve(__dirname, 'index.html'),
        login: resolve(__dirname, 'login.html'),
        contact: resolve(__dirname, 'contact.html'),
        profile: resolve(__dirname, 'profile.html'),
        settings: resolve(__dirname, 'settings.html'),
        notFound: resolve(__dirname, '404.html'),
        adminDashboard: resolve(__dirname, 'admin/dashboard.html'),
        courierDashboard: resolve(__dirname, 'courier/dashboard.html'),
        courierManifest: resolve(__dirname, 'courier/manifest.html'),
        merchantDashboard: resolve(__dirname, 'merchant/dashboard.html'),
        merchantShipments: resolve(__dirname, 'merchant/shipments.html'),
        merchantShipmentDetails: resolve(__dirname, 'merchant/shipment-details.html'),
        merchantCreateShipment: resolve(__dirname, 'merchant/create-shipment.html'),
        ownerDashboard: resolve(__dirname, 'owner/dashboard.html'),
        ownerEmployees: resolve(__dirname, 'owner/employees.html'),
        ownerMerchants: resolve(__dirname, 'owner/merchants.html'),
        ownerPayouts: resolve(__dirname, 'owner/payouts.html'),
        ownerPricing: resolve(__dirname, 'owner/pricing.html'),
        ownerReports: resolve(__dirname, 'owner/reports.html'),
        ownerSettings: resolve(__dirname, 'owner/settings.html'),
        ownerShipments: resolve(__dirname, 'owner/shipments.html'),
        ownerZones: resolve(__dirname, 'owner/zones.html'),
        ownerReportsCouriers: resolve(__dirname, 'owner/reports/couriers.html'),
        ownerReportsMerchants: resolve(__dirname, 'owner/reports/merchants.html'),
        ownerReportsWarehouse: resolve(__dirname, 'owner/reports/warehouse.html'),
        warehouseDashboard: resolve(__dirname, 'warehouse/dashboard.html'),
      },
    },
  },
  server: {
    port: 5173,
    open: true,
  },
  css: {
    devSourcemap: true,
  },
});