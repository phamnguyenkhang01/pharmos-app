import { Product } from "@/types/product";
import { createSlice, PayloadAction } from "@reduxjs/toolkit";

export interface CartItem {
    productId: number;
    nameEn: string;
    price: number;
    quantity: number;
}

interface CartState {
    items: CartItem[];
}

const initialState: CartState = { items: [] };

const cartSlice = createSlice({
    name: "cart",
    initialState,
    reducers: {
        hydrate(state, action: PayloadAction<CartItem[]>) {
            state.items = action.payload;
        },
        addItem(state, action: PayloadAction<Product>) {
            const product = action.payload;
            const existing = state.items.find((i) => i.productId === product.id);
            if (existing) {
                existing.quantity += 1;
            } else {
                state.items.push({
                    productId: product.id,
                    nameEn: product.nameEn,
                    price: product.price,
                    quantity: 1,
                })
            }
        },
        removeItem(state, action: PayloadAction<number>) {
            state.items = state.items.filter((i) => i.productId !== action.payload);
        }
    }
})

export const { hydrate, addItem, removeItem } = cartSlice.actions;
export default cartSlice.reducer;