"use client";

import { Product } from "@/types/product";
import { Button } from "@/components/ui/button";
import { useAppDispatch } from "@/lib/hooks";
import { addItem } from "@/lib/cart-slice";
import { stockStatus } from "@/lib/stock";
import { cn } from "@/lib/utils";

export function AddToCartButton({ product, className} : { product: Product; className?: string }) {
    const dispatch = useAppDispatch();
    const outOfStock = stockStatus(product) === "out-of-stock";

      return (
    <Button
      className={cn("w-full", className)}
      disabled={outOfStock}
      onClick={() => dispatch(addItem(product))}
    >
      {outOfStock ? "Out of stock" : "Add to cart"}
    </Button>
  );
}