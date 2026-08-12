"use client";

import { fetchCategories, fetchProducts } from "@/lib/api";
import { useInfiniteQuery, useQuery } from "@tanstack/react-query";
import { usePathname, useSearchParams, useRouter } from "next/navigation";
import { ProductCard } from "@/components/product-card";
import { Button } from "@/components/ui/button";

const PAGE_SIZE = 20;

export function ProductBrowser() {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();

  const keyword = searchParams.get("keyword") ?? "";
  const categoryId = searchParams.get("categoryId") ?? "";
  const minPrice = searchParams.get("minPrice") ?? "";
  const maxPrice = searchParams.get("maxPrice") ?? "";
  const inStockOnly = searchParams.get("inStockOnly") === "true";

  function updateParams(updates: Record<string, string | null>) {
    const params = new URLSearchParams(searchParams.toString());
    for (const [key, value] of Object.entries(updates)) {
      if (value === null || value === "") {
        params.delete(key);
      } else {
        params.set(key, value);
      }
    }
    router.replace(`${pathname}?${params.toString()}`, { scroll: false });
  }

  const categoriesQuery = useQuery({
    queryKey: ["categories"],
    queryFn: fetchCategories,
  });

  const productsQuery = useInfiniteQuery({
    queryKey: ["products", { keyword, categoryId, minPrice, maxPrice, inStockOnly }],
    queryFn: ({ pageParam }) =>
      fetchProducts({
        keyword: keyword || undefined,
        categoryId: categoryId || undefined,
        minPrice: minPrice || undefined,
        maxPrice: maxPrice || undefined,
        inStockOnly,
        page: pageParam,
        size: PAGE_SIZE,
      }),
    initialPageParam: 0,
    getNextPageParam: (lastPage, allPages) =>
      lastPage.length === PAGE_SIZE ? allPages.length : undefined,
  });

  const products = productsQuery.data?.pages.flat() ?? [];
  const otherCategories = categoriesQuery.data?.filter((c) => String(c.id) !== categoryId) ?? [];
  const activeCategoryName = categoriesQuery.data?.find((c) => String(c.id) === categoryId)?.nameEn;

  return (
    <div className="max-w-6xl mx-auto p-4 sm:p-6 flex flex-col gap-6">
      <div className="flex flex-col gap-1">
        <h1 className="text-2xl font-semibold">
          {keyword ? `Results for "${keyword}"` : activeCategoryName ?? "All products"}
        </h1>
        {!productsQuery.isLoading && (
          <p className="text-sm text-muted-foreground">
            {products.length} product{products.length === 1 ? "" : "s"}
            {productsQuery.hasNextPage ? "+" : ""}
          </p>
        )}
      </div>

      <div className="flex flex-col gap-3">
        <div className="flex gap-2 flex-wrap">
          <Button
            variant={categoryId === "" ? "default" : "outline"}
            size="sm"
            onClick={() => updateParams({ categoryId: null })}
          >
            All
          </Button>
          {categoriesQuery.data?.map((c) => (
            <Button
              key={c.id}
              variant={categoryId === String(c.id) ? "default" : "outline"}
              size="sm"
              onClick={() => updateParams({ categoryId: String(c.id) })}
            >
              {c.nameEn}
            </Button>
          ))}
        </div>

        <div className="flex flex-col sm:flex-row gap-2 sm:items-center">
          <input
            type="text"
            placeholder="Search products..."
            value={keyword}
            onChange={(e) => updateParams({ keyword: e.target.value })}
            className="border rounded-md px-2.5 py-1.5 text-sm bg-background sm:max-w-xs"
          />
          <div className="flex gap-2 items-center flex-wrap">
            <div className="flex items-center gap-1.5">
              <input
                type="number"
                placeholder="Min"
                value={minPrice}
                onChange={(e) => updateParams({ minPrice: e.target.value })}
                className="border rounded-md px-2.5 py-1.5 text-sm bg-background w-20"
              />
              <span className="text-muted-foreground text-sm">–</span>
              <input
                type="number"
                placeholder="Max"
                value={maxPrice}
                onChange={(e) => updateParams({ maxPrice: e.target.value })}
                className="border rounded-md px-2.5 py-1.5 text-sm bg-background w-20"
              />
            </div>
            <label className="flex items-center gap-1.5 text-sm">
              <input
                type="checkbox"
                checked={inStockOnly}
                onChange={(e) => updateParams({ inStockOnly: e.target.checked ? "true" : null })}
              />
              In stock only
            </label>
          </div>
        </div>
      </div>

      {productsQuery.isLoading && <p className="text-muted-foreground">Loading...</p>}
      {productsQuery.error && (
        <p className="text-destructive">Error: {(productsQuery.error as Error).message}</p>
      )}

      {!productsQuery.isLoading && products.length === 0 && (
        <div className="border border-dashed rounded-lg p-10 text-center flex flex-col items-center gap-3">
          <p className="font-medium">No products match your filters</p>
          <p className="text-sm text-muted-foreground">
            Try a different keyword or clear a filter, or browse a category below.
          </p>
          <div className="flex gap-2 flex-wrap justify-center">
            {otherCategories.map((c) => (
              <Button
                key={c.id}
                variant="outline"
                size="sm"
                onClick={() => updateParams({ categoryId: String(c.id), keyword: null })}
              >
                {c.nameEn}
              </Button>
            ))}
          </div>
        </div>
      )}

      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
        {products.map((p) => (
          <ProductCard key={p.id} product={p} />
        ))}
      </div>

      {productsQuery.hasNextPage && (
        <Button
          variant="outline"
          className="self-center"
          onClick={() => productsQuery.fetchNextPage()}
          disabled={productsQuery.isFetchingNextPage}
        >
          {productsQuery.isFetchingNextPage ? "Loading..." : "Load more"}
        </Button>
      )}
    </div>
  );
}
