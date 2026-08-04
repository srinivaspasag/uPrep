"use client";

import { useParams } from "next/navigation";
import TestForm from "../../TestForm";

export default function EditTestPage() {
  const params = useParams();
  return <TestForm testId={String(params.id)} />;
}
