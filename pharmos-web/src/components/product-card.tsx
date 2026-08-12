import Link from "next/link";
import { Product } from "@/types/product";
import { STOCK_BADGE_CLASSES, STOCK_LABEL, stockStatus } from "@/lib/stock";
import { ProductBadges } from "@/components/product-badges";
import { PillBottle } from "lucide-react";
import { AddToCartButton } from "./add-to-cart-button";

export function ProductCard({ product }: { product: Product }) {
  const status = stockStatus(product);

  return (
    <div className="flex flex-col border rounded-lg overflow-hidden bg-card text-card-foreground">
      <Link href={`/products/${product.id}`} className="flex flex-col flex-1">
        <div className="aspect-square flex items-center justify-center bg-muted text-muted-foreground">
          <PillBottle className="size-10" />
        </div>
        <div className="flex flex-col gap-1 p-3 flex-1">
          <span className="text-xs uppercase text-muted-foreground">
            {product.category.nameEn}
          </span>
          <ProductBadges product={product} />
          <span className="text-sm font-semibold leading-tight">{product.nameEn}</span>
          {product.nameVi && (
            <span className="text-sm italic text-muted-foreground">{product.nameVi}</span>
          )}
          <div className="flex items-center justify-between mt-auto pt-2">
            <span className="font-semibold">${product.price.toFixed(2)}</span>
            <span className={`text-[11px] px-1.5 py-0.5 rounded border ${STOCK_BADGE_CLASSES[status]}`}>
              {STOCK_LABEL[status]}
            </span>
          </div>
        </div>
      </Link>
      <div className="p-3 pt-0">
          <AddToCartButton product={product} />
      </div>
    </div>
  );
}
