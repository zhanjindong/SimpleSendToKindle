using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Diagnostics;

namespace Startup
{
    class Program
    {
        static void Main(string[] args)
        {
            try
            {
                if (!Directory.Exists(System.AppDomain.CurrentDomain.BaseDirectory + "\\log"))
                {
                    Directory.CreateDirectory(System.AppDomain.CurrentDomain.BaseDirectory + "\\log");
                }

                if (args.Length == 0)
                {
                    WriteStandardStreamOut("Missing parameter.");
                    Log2File("Missing parameter.");
                    return;
                }

                string url = ReadStandardStreamIn();
                Log2File("Running SimpleSendToKindle.jar with url:" + url);
                string ret = RunJar(url);
                Log2File("Completed with return msg:" + ret);
                WriteStandardStreamOut("{\"text\":\"" + ret + "\"}");
            }
            catch (Exception ex)
            {
                Log2File("Error:" + ex.ToString());
                WriteStandardStreamOut("{\"text\":\"" + "Error." + ex.Message + "\"}");
            }
        }

        static string RunJar(string arg)
        {
            ProcessStartInfo startInfo = new ProcessStartInfo()
            {
                WorkingDirectory = System.AppDomain.CurrentDomain.BaseDirectory,
                UseShellExecute = false,//要重定向 IO 流，Process 对象必须将 UseShellExecute 属性设置为 False。
                CreateNoWindow = true,
                RedirectStandardOutput = true,
                //RedirectStandardInput = false,
                WindowStyle = ProcessWindowStyle.Normal,
                FileName = "java.exe",
                Arguments = @" -Dfile.encoding=utf-8 -jar SimpleSendToKindle.jar " + arg,
            };
            //启动进程
            using (Process process = Process.Start(startInfo))
            {
                using (StreamReader reader = process.StandardOutput)
                {
                    return reader.ReadToEnd();
                }
            }
        }

        static void Log2File(string s)
        {
            FileStream fs = new FileStream(System.AppDomain.CurrentDomain.BaseDirectory + @"log/startup.log", FileMode.Append);
            StreamWriter sw = new StreamWriter(fs, Encoding.UTF8);
            sw.WriteLine(s);
            sw.Close();
            fs.Close();
        }

        static string ReadStandardStreamIn()
        {
            using (Stream stdin = Console.OpenStandardInput())
            {
                int length = 0;
                byte[] bytes = new byte[4];
                stdin.Read(bytes, 0, 4);
                length = System.BitConverter.ToInt32(bytes, 0);

                byte[] msgBytes = new byte[length];
                stdin.Read(msgBytes, 0, length);

                string decodeMsg = Microsoft.JScript.GlobalObject.decodeURI(System.Text.Encoding.UTF8.GetString(msgBytes));
                return decodeMsg;
            }
        }

        static void WriteStandardStreamOut(string msg)
        {
            int length = msg.Length;
            byte[] lenBytes = System.BitConverter.GetBytes(length);
            byte[] msgBytes = System.Text.Encoding.UTF8.GetBytes(msg);
            byte[] wrapBytes = new byte[4 + length];
            Array.Copy(lenBytes, 0, wrapBytes, 0, 4);
            Array.Copy(msgBytes, 0, wrapBytes, 4, length);

            using (Stream stdout = Console.OpenStandardOutput())
            {
                stdout.Write(wrapBytes, 0, wrapBytes.Length);
            }
        }
    }
}
