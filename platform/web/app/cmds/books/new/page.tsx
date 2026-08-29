import CmdsUploadForm from "@/components/CmdsUploadForm";

export default function NewBookPage() {
  return (
    <CmdsUploadForm
      kind="book"
      title="Add a Book"
      accept=".pdf"
      hint="PDF textbook or reference book — tag it to a chapter so students can find it"
    />
  );
}
