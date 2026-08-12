import { notFound } from "next/navigation";
import Link from "next/link";
import { fetchProduct } from "@/lib/api";
import { AddToCartButton } from "@/components/add-to-cart-button";
import { ProductBadges } from "@/components/product-badges";
import { STOCK_BADGE_CLASSES, STOCK_LABEL, stockStatus } from "@/lib/stock";
import { PillBottle } from "lucide-react";

export default async function ProductPage({
    params,
}: {
    params: Promise<{ id: string}>;
}) {
    const { id } = await params;
    const product = await fetchProduct(id);
    if (!product) notFound();

    const status = stockStatus(product);

      return (
    <div className="max-w-4xl mx-auto p-4 flex flex-col gap-4">
      <Link href="/" className="text-sm underline text-muted-foreground w-fit">
        &larr; Back to results
      </Link>
      <div className="flex flex-wrap gap-6">
        <div className="w-full sm:w-64 aspect-square flex items-center justify-center bg-muted text-muted-foreground rounded-lg shrink-0">
          <PillBottle className="size-16" />
        </div>
        <div className="flex flex-col gap-2 flex-1 min-w-64">
          <span className="text-xs uppercase text-muted-foreground">
            {product.category.nameEn}
          </span>
          <h1 className="text-2xl sm:text-3xl font-semibold">{product.nameEn}</h1>
          {product.nameVi && <p className="italic text-muted-foreground">{product.nameVi}</p>}
          <div className="flex items-center gap-2 flex-wrap">
            <span className="text-xl sm:text-2xl font-semibold">${product.price.toFixed(2)}</span>
            <span className={`text-[11px] px-1.5 py-0.5 rounded border ${STOCK_BADGE_CLASSES[status]}`}>
              {STOCK_LABEL[status]}
            </span>
            <ProductBadges product={product} />
          </div>
          <AddToCartButton product={product} className="w-fit" />
          {product.descriptionEn && (
            <div className="mt-4">
              <h2 className="text-xs uppercase text-muted-foreground mb-1">Description</h2>
              <p className="max-w-prose">{product.descriptionEn}</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}