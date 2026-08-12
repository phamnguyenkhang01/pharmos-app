import { Suspense } from "react";
import { ProductBrowser } from "./product-browser";

export default function Home() {
  return (
    <Suspense fallback={<p>Loading...</p>}>
      <ProductBrowser />
    </Suspense>
  )
}