@echo off

echo Testing Zhipu AI API...
echo.

echo Request body:
echo {"model":"glm-4","messages":[{"role":"user","content":"你好"}]}
echo.

echo Response:
curl -X POST "https://open.bigmodel.cn/api/paas/v4/chat/completions" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer 34d3b132ecaa4d6ab1cbc9a3e0c2f8b8.GN6o4XELNzF9IQ1S" ^
  -d "{\"model\":\"glm-4\",\"messages\":[{\"role\":\"user\",\"content\":\"你好\"}]}"

echo.
echo Test completed.