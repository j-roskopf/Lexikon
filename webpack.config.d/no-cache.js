// Development rebuilds must be observable without clearing browser cache.
config.devServer = config.devServer || {};
config.devServer.headers = {
  ...(config.devServer.headers || {}),
  "Cache-Control": "no-store, no-cache, must-revalidate",
  "Pragma": "no-cache",
  "Expires": "0",
};
