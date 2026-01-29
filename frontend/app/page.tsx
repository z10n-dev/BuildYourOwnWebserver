import Image from "next/image";
import Button from "./components/Button";
import HTTPSchemaImage from "./http-schema.png";
import { HoverCard } from "./components/HoverCard";

export default function Home() {
  return (
    <div className="flex flex-col min-h-screen items-center bg-zinc-50 font-sans dark:bg-black mx-4">
      <h1 className="text-5xl font-bold text-gray-900 dark:text-white mt-40 text-center">See the invisible side of HTTP</h1>
      <p className="px-20 text-center text-2xl text-gray-700 dark:text-gray-300 my-10">Most server hide their complexity. Glass Box exposes it. Wath requests flow throuhg parsing, routing, and threading in real-time</p>
      <Button href="/live-dashboard" label="Live Dashboard" target="_self" />
      <Image src={HTTPSchemaImage} alt="Description"className="my-10 rounded"/>
      <div className="flex flex-col bg-slate-900 size-full items-center">
        <h2 className="text-3xl font-bold text-gray-900 dark:text-white mt-20">Why this project?</h2>
        <p className="px-20 text-center text-xl text-gray-700 dark:text-gray-300 my-10">Built strictly with Java Standard Library. No Spring, No Netty, No Magic.</p>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 my-10 mx-4 md:mx-20">
          <HoverCard className="">
          <h1 className="text-4xl font bold mb-4">🔎</h1>
          <h3 className="text-2xl font-bold mb-4 text-white">Educational Purpose</h3>
          <p className="text-lg text-gray-300">This project is designed to help developers understand the inner workings of HTTP servers by providing a transparent view of request handling, parsing, routing, and threading.</p>
        </HoverCard>
        <HoverCard className="">
          <h1 className="text-4xl font bold mb-4">⚡</h1>
          <h3 className="text-2xl font-bold mb-4 text-white">Educational Purpose</h3>
          <p className="text-lg text-gray-300">This project is designed to help developers understand the inner workings of HTTP servers by providing a transparent view of request handling, parsing, routing, and threading.</p>
        </HoverCard>
        <HoverCard className="">
          <h1 className="text-4xl font bold mb-4">📊</h1>
          <h3 className="text-2xl font-bold mb-4 text-white">Educational Purpose</h3>
          <p className="text-lg text-gray-300">This project is designed to help developers understand the inner workings of HTTP servers by providing a transparent view of request handling, parsing, routing, and threading.</p>
        </HoverCard>
        <HoverCard className="">
          <h1 className="text-4xl font bold mb-4">🛠️</h1>
          <h3 className="text-2xl font-bold mb-4 text-white">Educational Purpose</h3>
          <p className="text-lg text-gray-300">This project is designed to help developers understand the inner workings of HTTP servers by providing a transparent view of request handling, parsing, routing, and threading.</p>
        </HoverCard>
        </div>
      </div>
    </div>
  );
}
