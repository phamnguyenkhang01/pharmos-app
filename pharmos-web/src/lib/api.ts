import { Category, Product } from "@/types/product";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL;

export interface ProductFilters {
    keyword?: string;
    categoryId?: string;
    minPrice?: string;
    maxPrice?: string;
    inStockOnly?: boolean;
    page?: number;
    size?: number;
}

export async function fetchProducts(filters:ProductFilters = {}) : Promise<Product[]> {
    const params = new URLSearchParams();
    if (filters.keyword) params.set("keyword", filters.keyword);
    if (filters.categoryId) params.set("categoryId", filters.categoryId);
    if (filters.minPrice) params.set("minPrice", filters.minPrice);
    if (filters.maxPrice) params.set("maxPrice", filters.maxPrice);
    if (filters.inStockOnly) params.set("inStockOnly", "true");
    if (filters.page !== undefined) params.set("page", String(filters.page));
  if (filters.size !== undefined) params.set("size", String(filters.size));

    const query = params.toString();
    const res = await fetch(`${API_BASE_URL}/api/products${query ? `?${query}` : ""}`);
    if (!res.ok) throw new Error(`Failed to fetch products: ${res.status}`);
    return res.json();
}

export async function fetchCategories() : Promise<Category[]> {
    const res = await fetch(`${API_BASE_URL}/api/categories`);
  if (!res.ok) throw new Error(`Failed to fetch categories: ${res.status}`);
    return res.json();   
}

export async function fetchProduct(id: string): Promise<Product | null> {
  const res = await fetch(`${API_BASE_URL}/api/products/${id}`);
  if (res.status === 404) return null;
  if (!res.ok) throw new Error(`Failed to fetch product: ${res.status}`);
  return res.json();
}