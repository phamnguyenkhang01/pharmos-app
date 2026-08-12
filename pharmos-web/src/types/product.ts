export interface Category {
    id: number;
    nameEn: string;
    nameVi: string | null;
    sortOrder: number | null;
}

export interface Product {
  id: number;
  nameEn: string;
  nameVi: string | null;
  descriptionEn: string | null;
  descriptionVi: string | null;
  category: Category;
  price: number;
  imageRef: string | null;
  stockQuantity: number;
  status: "DRAFT" | "PUBLISHED" | "UNPUBLISHED";
  medication: boolean;
  restricted: boolean;
}