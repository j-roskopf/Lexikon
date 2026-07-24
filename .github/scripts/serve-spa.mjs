import { createServer } from 'node:http';
import { readFileSync, existsSync } from 'node:fs';
import { join, extname } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = join(fileURLToPath(new URL('.', import.meta.url)), '../../build/dist/wasmJs/productionExecutable');
const port = Number(process.env.LEXIKON_WEB_PORT ?? 10001);

const mime = {
  '.html': 'text/html; charset=utf-8',
  '.js': 'text/javascript; charset=utf-8',
  '.wasm': 'application/wasm',
  '.json': 'application/json',
};

createServer((req, res) => {
  let path = req.url?.split('?')[0] ?? '/';
  if (path === '/') path = '/index.html';
  const file = join(root, path);
  const resolved = existsSync(file) && !file.includes('..') ? file : join(root, 'index.html');
  const body = readFileSync(resolved);
  const ext = extname(resolved);
  res.writeHead(200, { 'Content-Type': mime[ext] ?? 'application/octet-stream', 'Cache-Control': 'no-cache' });
  res.end(body);
}).listen(port, '127.0.0.1', () => {
  console.log(`Lexikon SPA at http://127.0.0.1:${port}`);
});
