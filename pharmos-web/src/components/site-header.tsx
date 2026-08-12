"use client";

import Link from "next/link";
import { useAppSelector } from "@/lib/hooks";

export function SiteHeader() {
    const count = useAppSelector((state) => 
        state.cart.items.reduce((sum, item) => sum + item.quantity, 0)
    );
    return (
        <header className="border-b">
          <div className="max-w-6xl mx-auto px-4 sm:px-6 py-3 flex items-center justify-between">
            <Link href="/" className="text-lg font-semibold">
                PharmOS Storefront
            </Link>
            <span className="text-sm border rounded-md px-2.5 py-1">Cart: {count}</span>
          </div>
        </header>
  );
}