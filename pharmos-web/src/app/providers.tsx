"use client";

import { type AppStore, makeStore } from "@/lib/store";
import { hydrate } from "@/lib/cart-slice";
import { QueryClient, QueryClientProvider} from "@tanstack/react-query";
import { useEffect, useState } from "react";
import { Provider } from "react-redux";

const CART_STORAGE_KEY = "pharmos-cart";

export function Providers({ children }: { children: React.ReactNode}) {
    const [queryClient] = useState(() => new QueryClient());
    const [store] = useState<AppStore>(() => makeStore());

    useEffect(() => {
        const saved = localStorage.getItem(CART_STORAGE_KEY);
        if (saved) {
            store.dispatch(hydrate(JSON.parse(saved)));
        }
        return store.subscribe(() => {
            localStorage.setItem(CART_STORAGE_KEY, JSON.stringify(store.getState().cart.items));
        })
    }, [store]);

    return (
        <QueryClientProvider client={queryClient}>
            <Provider store={store}>
                {children}
            </Provider>
        </QueryClientProvider>
    );
}