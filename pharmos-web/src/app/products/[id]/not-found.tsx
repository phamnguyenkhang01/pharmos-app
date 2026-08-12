import Link from "next/link";

export default function ProductNotFound() {
  return (
    <div className="max-w-4xl mx-auto p-10 text-center flex flex-col items-center gap-3">
      <h1 className="text-xl font-semibold">Product not found</h1>
      <p className="text-muted-foreground">
        This product doesn&apos;t exist or is no longer available.
      </p>
      <Link href="/" className="underline text-sm">
        &larr; Back to all products
      </Link>
    </div>
  );
}