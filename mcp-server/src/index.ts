import { Server } from "@modelcontextprotocol/sdk/server/index.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import {
  CallToolRequestSchema,
  ListToolsRequestSchema,
} from "@modelcontextprotocol/sdk/types.js";
import axios from "axios";

const JAVA_API_URL = process.env.JAVA_API_URL || "http://localhost:8080";

const server = new Server(
  {
    name: "taiwan-travel",
    version: "1.0.0",
  },
  {
    capabilities: {
      tools: {},
    },
  }
);

// List available tools
server.setRequestHandler(ListToolsRequestSchema, async () => {
  return {
    tools: [
      {
        name: "search_sights",
        description:
          "搜尋台灣的景點。支援按城市、區域、關鍵字搜尋。預設返回前20筆結果，避免資料過多。當使用者詢問「景點」、「哪裡好玩」、「推薦地方」等相關問題時使用此工具。",
        inputSchema: {
          type: "object",
          properties: {
            city: {
              type: "string",
              description: "城市名稱，例如：台北市、新北市、台中市",
            },
            zone: {
              type: "string",
              description: "區域名稱，例如：板橋區、中正區（不要加市/縣，只寫區域）",
            },
            keyword: {
              type: "string",
              description: "搜尋關鍵字，例如：博物館、公園、夜市",
            },
            limit: {
              type: "number",
              description: "返回筆數上限，預設20，最大100。如需更多結果可增加此值",
              default: 20,
            },
          },
        },
      },
      {
        name: "search_foods",
        description:
          "搜尋台灣的美食和餐廳。支援按關鍵字、區域搜尋。預設返回前20筆結果。當使用者詢問「哪裡有好吃的」、「餐廳推薦」、「美食」等相關問題時使用此工具。",
        inputSchema: {
          type: "object",
          properties: {
            keyword: {
              type: "string",
              description: "搜尋關鍵字，例如：拉麵、火鍋、咖啡（可包含城市名或區域）",
            },
            zone: {
              type: "string",
              description: "區域名稱，例如：信義區、大安區",
            },
            limit: {
              type: "number",
              description: "返回筆數上限，預設20，最大100",
              default: 20,
            },
          },
        },
      },
      {
        name: "search_festivals",
        description:
          "搜尋台灣的節慶活動和節慶。支援按狀態（進行中/即將開始）、區域、關鍵字搜尋。預設返回前20筆結果。當使用者詢問「有什麼活動」、「節慶」、「音樂祭」、「燈會」等相關問題時使用此工具。",
        inputSchema: {
          type: "object",
          properties: {
            status: {
              type: "string",
              enum: ["ongoing", "upcoming", "all"],
              description:
                "活動狀態：ongoing(進行中)、upcoming(即將開始)、all(全部)。當使用者問「最近」或「現在」時使用 ongoing",
              default: "all",
            },
            zone: {
              type: "string",
              description: "區域名稱，例如：板橋區、中正區（不要加市/縣，只寫區域）",
            },
            keyword: {
              type: "string",
              description: "搜尋關鍵字，例如：燈會、音樂祭、台北（城市名也可作為關鍵字）",
            },
            days: {
              type: "number",
              description: "搜尋未來幾天內的活動（僅用於 upcoming，預設30天）",
              default: 30,
            },
            limit: {
              type: "number",
              description: "返回筆數上限，預設20，最大100",
              default: 20,
            },
          },
        },
      },
      {
        name: "get_sight_details",
        description: "取得指定景點的詳細資訊，包含完整描述、地址、營業時間等。",
        inputSchema: {
          type: "object",
          properties: {
            id: {
              type: "number",
              description: "景點 ID",
            },
          },
          required: ["id"],
        },
      },
      {
        name: "get_food_details",
        description:
          "取得指定餐廳的詳細資訊，包含 Google Places 的詳細資料、營業時間、評論等。",
        inputSchema: {
          type: "object",
          properties: {
            id: {
              type: "number",
              description: "餐廳 ID",
            },
          },
          required: ["id"],
        },
      },
    ],
  };
});

// Handle tool calls
server.setRequestHandler(CallToolRequestSchema, async (request) => {
  const { name, arguments: args = {} } = request.params;

  try {
    switch (name) {
      case "search_sights": {
        const params = new URLSearchParams();
        if (args.city) params.append("city", args.city as string);
        if (args.zone) params.append("zone", args.zone as string);
        if (args.keyword) params.append("keyword", args.keyword as string);
        if (args.limit !== undefined)
          params.append("limit", String(args.limit));

        const response = await axios.get(
          `${JAVA_API_URL}/sights?${params.toString()}`
        );

        return {
          content: [
            {
              type: "text",
              text: JSON.stringify(response.data, null, 2),
            },
          ],
        };
      }

      case "search_foods": {
        const params = new URLSearchParams();
        if (args.keyword) params.append("keyword", args.keyword as string);
        if (args.zone) params.append("zone", args.zone as string);
        if (args.limit !== undefined)
          params.append("limit", String(args.limit));

        const response = await axios.get(
          `${JAVA_API_URL}/foods?${params.toString()}`
        );

        return {
          content: [
            {
              type: "text",
              text: JSON.stringify(response.data, null, 2),
            },
          ],
        };
      }

      case "search_festivals": {
        const params = new URLSearchParams();
        if (args.status) params.append("status", args.status as string);
        if (args.zone) params.append("zone", args.zone as string);
        if (args.keyword) params.append("keyword", args.keyword as string);
        if (args.days !== undefined)
          params.append("days", String(args.days));
        if (args.limit !== undefined)
          params.append("limit", String(args.limit));

        const response = await axios.get(
          `${JAVA_API_URL}/festivals?${params.toString()}`
        );

        return {
          content: [
            {
              type: "text",
              text: JSON.stringify(response.data, null, 2),
            },
          ],
        };
      }

      case "get_sight_details": {
        const response = await axios.get(
          `${JAVA_API_URL}/sights/${args.id}`
        );

        return {
          content: [
            {
              type: "text",
              text: JSON.stringify(response.data, null, 2),
            },
          ],
        };
      }

      case "get_food_details": {
        const response = await axios.get(
          `${JAVA_API_URL}/foods/${args.id}/details`
        );

        return {
          content: [
            {
              type: "text",
              text: JSON.stringify(response.data, null, 2),
            },
          ],
        };
      }

      default:
        throw new Error(`Unknown tool: ${name}`);
    }
  } catch (error: any) {
    return {
      content: [
        {
          type: "text",
          text: `Error: ${error.message || "Unknown error occurred"}`,
        },
      ],
      isError: true,
    };
  }
});

// Start the server
async function main() {
  const transport = new StdioServerTransport();
  await server.connect(transport);
}

main().catch((error) => {
  process.stderr.write(`Fatal error: ${error}\n`);
  process.exit(1);
});
