import { Product } from "@/types/product";

export const LOW_STOCK_THRESHOLD = 10;

export type StockStatus = "in-stock" | "low-stock" | "out-of-stock";

export function stockStatus(product: Pick<Product, "stockQuantity">): StockStatus {
    if (product.stockQuantity <= 0 ) return "out-of-stock";
    if (product.stockQuantity < LOW_STOCK_THRESHOLD) return "low-stock";
    return "in-stock";
}

export const STOCK_LABEL: Record<StockStatus, string> = {
  "in-stock": "In stock",
  "low-stock": "Low stock",
  "out-of-stock": "Out of stock",
};

export const STOCK_BADGE_CLASSES: Record<StockStatus, string> = {
  "in-stock":
    "bg-green-50 text-green-700 border-green-200 dark:bg-green-950 dark:text-green-400 dark:border-green-900",
  "low-stock":
    "bg-amber-50 text-amber-700 border-amber-200 dark:bg-amber-950 dark:text-amber-400 dark:border-amber-900",
  "out-of-stock":
    "bg-red-50 text-red-700 border-red-200 dark:bg-red-950 dark:text-red-400 dark:border-red-900",
}