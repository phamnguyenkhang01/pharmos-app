import { Product } from "@/types/product";

export function ProductBadges({ product }: { product: Pick<Product, "medication" | "restricted"> }) {
    if (!product.medication && !product.restricted) return null;
    return (
        <div className="flex gap-1 flex-wrap">
            {product.medication && (
                <span className="text-[11px] px-1.5 py-0.5 rounded border bg-blue-50 text-blue-700 border-blue-200 dark:bg-blue-950 dark:text-blue-400 dark:border-blue-900">
                    Rx/OTC
                </span>
            )}
            {product.restricted && (
                <span className="text-[11px] px-1.5 py-0.5 rounded border bg-red-50 text-red-700 border-red-200 dark:bg-red-950 dark:text-red-400 dark:border-red-900">
                    Age-restricted
                </span>
            )}
        </div>
    )
}