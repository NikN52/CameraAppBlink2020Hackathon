package com.example.camtest;
import android.os.Environment;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.time.Clock;

import java.util.*;


public class Brain {

    static ArrayList<Mat> transform_video(String path_to_video, int submat_radius) {
        /*
        Метод возвращает массив картинок из видео

        path_to_video example: "Videos/test2.mp4"
        duration_frames example: 500 frames = FPS * video duration in seconds
        */

        // При необходимости выгрузить картинки, раскомментировать код:
        // Папка с кучей картинок
        //String output = "Images";



        VideoCapture cap = new VideoCapture();
        cap.open(path_to_video);

        int video_length = (int) cap.get(Videoio.CAP_PROP_FRAME_COUNT);
        int frames_per_second = (int) cap.get(Videoio.CAP_PROP_FPS);
        int frame_number = (int) cap.get(Videoio.CAP_PROP_POS_FRAMES);

        Mat frame = new Mat();


        ArrayList<Mat> video = new ArrayList<Mat>();

        if (cap.isOpened())
        {
            System.out.println("Видео успешно открыто!");
            System.out.println("Общее количество кадров: " + video_length);
            System.out.println("Количество кадров в секунду: "+frames_per_second);
            System.out.println("Предварительная обрезка кадров видео...");


            while(cap.read(frame)) //the last frame of the movie will be invalid. check for it !
            {
                // Раскомментировать код при необходимости записи в файл
                // Imgcodecs.imwrite(output + "/" + frame_number +".jpg", frame);[450:-450, 800:-900
//                frame = frame.submat(450, -450, 800, -900);

                Mat buffer_image = new Mat();
                int center_x = (int) frame.width()/2;
                int center_y = (int) frame.height()/2;

                int left_border = center_x-submat_radius;
                int right_border = center_x+submat_radius;
                int up_border = center_y-submat_radius;
                int down_border = center_y+submat_radius;
                buffer_image = frame.submat(up_border, down_border, left_border, right_border);

//                HighGui.imshow("cock2", buffer_image.clone());
//                HighGui.waitKey(10);
                video.add(get_rotate(buffer_image.clone(),-90));
                frame_number++;
            }
            cap.release();
        }

        else
        {
            System.out.println("Fail");
        }


        System.out.println("------------------------------------------");
        System.out.println("Средняя насыщенность кадра: " + (int) Core.mean(video.get(5).clone()).val[1]);
        System.out.println("Средняя яркость кадра: "+ (int) Core.mean(video.get(5).clone()).val[2]);
        System.out.println("------------------------------------------");

        return video;
    }

    static ArrayList<ArrayList<Double>> update_df(ArrayList<ArrayList<Double>> df_total, ArrayList<Integer> df0, double counter)
    {
        /*
            Метод прикрепляет к массивам в массиве элементы массивов
        */

        ArrayList<Integer> df_row_no_time = new ArrayList<Integer>();
        ArrayList<Double> df_row = new ArrayList<Double>();
        ArrayList<ArrayList<Double>> total = new ArrayList<ArrayList<Double>>(df_total);
        ArrayList<Double> temp_column = new ArrayList<Double>();

        df_row.add(0, counter);

        for (int i=0; i<df0.size(); i++)
        {
            df_row.add(Double.valueOf(df0.get(i)));
        }

        if (total.size() != 0)
        {
            for (int i=0; i<df_total.size(); i++)
            {
                temp_column = new ArrayList<Double>(df_total.get(i));
                Double current_value = df_row.get(i);
                temp_column.add(current_value);
                total.set(i, temp_column);
            }
        }
        else
        {
            for (int i=0; i<df_row.size(); i++)
            {
                ArrayList<Double> current_value = new ArrayList<Double>();
                current_value.add(df_row.get(i));
                total.add(i, current_value);

            }

        }

        //System.out.println(total);
        return total;
    }

    static double my_round(double x, double base) {
        /*
        Метод возвращает целое количество вместившихся base фрэймов
        */
        double copies = x/base;
        double result = base* Math.round(copies);
        return (double) result;
    }

    static ArrayList<ArrayList<Integer>> get_signal_map(ArrayList<Integer> signal) {
        /*
        Метод возвращает упорядоченный сигнал по пакетам [HIGH],[LOW]
        */

        ArrayList<ArrayList<Integer>> s_map = new ArrayList<ArrayList<Integer>>();
        ArrayList<Integer> s = new ArrayList<Integer>(signal);
        ArrayList<Integer> s_strobe_high = new ArrayList<Integer>();
        ArrayList<Integer> s_strobe_low = new ArrayList<Integer>();

        for (int i=0; i<s.size()-1; i++)
        {
            if ((int) s.get(i+1)==0 && (int) s.get(i) == 1)
            {
                s_strobe_high.add(i);
                s_strobe_low.add(i+1);
                if (s_strobe_high.size() == 2)
                {
                    s_map.add(s_strobe_high);
                    s_strobe_high = new ArrayList<Integer>();
                }
                else
                {
                    s_strobe_high = new ArrayList<Integer>();
                }
            }
            if ((int) s.get(i+1)==1 && (int) s.get(i) == 0)
            {
                s_strobe_high.add(i+1);
                s_strobe_low.add(i);
                if (s_strobe_low.size() == 2)
                {
                    s_map.add(s_strobe_low);
                    s_strobe_low = new ArrayList<Integer>();
                }
                else
                {
                    s_strobe_low = new ArrayList<Integer>();
                }
            }

        }

        return s_map;
    }

    static int median(int[] a) {

        if (a == null || a.length == 0)
            return 0;

        Arrays.sort(a);

        int previous = a[0];
        int popular = a[0];
        int count = 1;
        int maxCount = 1;

        for (int i = 1; i < a.length; i++) {
            if (a[i] == previous)
                count++;
            else {
                if (count > maxCount) {
                    popular = a[i-1];
                    maxCount = count;
                }
                previous = a[i];
                count = 1;
            }
        }

        return count > maxCount ? a[a.length-1] : popular;

    }

    static String get_bit_state(ArrayList<Integer> signal, ArrayList<ArrayList<Integer>> signal_map, int start_index, int finish_index) {
        /*
        Метод возвращает состояние бита
        */
        int finish = 0;
        if (finish_index > signal.size())
        {
            finish = signal.size()-2;
        }
        else
        {
            finish = finish_index;
        }
//        System.out.println("signal: "+signal);
//        System.out.println( "start: "+ start_index + " finish: "+finish);
//        System.out.println( " signal_length: "+signal.size());
        ArrayList<Integer> bit = new ArrayList<Integer>(signal.subList(start_index, finish+1));
        double bit_state = 0.0;

        //System.out.println("signal_map: "+signal_map);
        int temp[] = new int[bit.size()];
        for (int i=0; i<bit.size();i++)
        {
            temp[i] = (int) bit.get(i);
        }
        bit_state = (double) median(temp);

        for (int i=0; i<signal_map.size(); i++)
        {
            if(signal_map.get(i).get(0) > start_index && signal_map.get(i).get(1) < finish_index)
            {
                bit_state = signal.get((int) signal_map.get(i).get(1));
            }
        }

        return String.valueOf((int) bit_state);
    }

    static String convert_code_to_text(ArrayList<String> signal_codes) {
        /*
        Метод конвертирует 8битные коды в символы
        */
        String result = new String();
        Integer binary;

        for (int i=0; i< signal_codes.size(); i++)
        {
            if (signal_codes.get(i).length() == 8)
            {
                binary = Integer.valueOf(signal_codes.get(i), 2);
//                System.out.println("binary: "+binary);
                char temp = (char) (int) binary;
                result += temp;
            }
        }
        return String.valueOf(result);
    }

    static ArrayList<Integer> convert_double_list_to_integer(ArrayList<Double> list)
    {
        ArrayList<Integer> result = new ArrayList<Integer>();
        for (int i=0; i<list.size(); i++)
        {
            result.add(list.get(i).intValue());
        }

        return result;
    }

    static String decode(ArrayList<ArrayList<Double>> df) {
        /*
        Метод возвращает переведенный текст из данных, собранных с изображений
        */

        ArrayList<Integer> s1 = new ArrayList<Integer>(convert_double_list_to_integer(df.get(1)));
        ArrayList<Integer> s2 = new ArrayList<Integer>(convert_double_list_to_integer(df.get(2)));
        ArrayList<Integer> s3 = new ArrayList<Integer>(convert_double_list_to_integer(df.get(3)));
        ArrayList<Integer> s4 = new ArrayList<Integer>(convert_double_list_to_integer(df.get(4)));
        ArrayList<Integer> s5 = new ArrayList<Integer>(convert_double_list_to_integer(df.get(5)));
        ArrayList<Integer> s6 = new ArrayList<Integer>(convert_double_list_to_integer(df.get(6)));
        ArrayList<Integer> s7 = new ArrayList<Integer>(convert_double_list_to_integer(df.get(7)));
        ArrayList<Integer> s8 = new ArrayList<Integer>(convert_double_list_to_integer(df.get(8)));
        ArrayList<Integer> s9 = new ArrayList<Integer>(convert_double_list_to_integer(df.get(9)));
        ArrayList<Integer> s10 = new ArrayList<Integer>(convert_double_list_to_integer(df.get(10)));

        ArrayList<Integer> d1 = new ArrayList<Integer>(convert_double_list_to_integer(df.get(11)));
        ArrayList<Integer> d2 = new ArrayList<Integer>(convert_double_list_to_integer(df.get(12)));
        ArrayList<Integer> d3 = new ArrayList<Integer>(convert_double_list_to_integer(df.get(13)));

//        System.out.println("s1: "+s1);

        ArrayList<Integer> d3_strobe = new ArrayList<Integer>();
        ArrayList<ArrayList<Integer>> d3_map = new ArrayList<ArrayList<Integer>>();
        Boolean permission = false;

        for (int i=0; i<d3.size()-1; i++)
        {
            if(d3.get(i+1) == 1 && d3.get(i) == 0)
            {
                d3_strobe.add(i);
                permission = true;
            }
            if(d3.get(i+1) == 0 && d3.get(i) == 1 && permission == true)
            {
                d3_strobe.add(i+1);
                d3_map.add(d3_strobe);
                d3_strobe = new ArrayList<Integer>();
                permission = false;
            }
        }
        ArrayList<Integer> tmp = new ArrayList<Integer>(2);
        int buffer = 0;
        int minimum = 0;
        int maximum = 0;
        int delta = 0;
//        System.out.println("d3_map "+d3_map);
        for (int i=0; i<d3_map.size(); i++)
        {
            minimum = d3_map.get(i).get(0);
            maximum = d3_map.get(i).get(1);
            delta = maximum - minimum;
            if (delta > buffer)
            {
                tmp.add(0, minimum);
                tmp.add(1, maximum);
            }
//            System.out.println("tmp "+tmp);
            buffer = delta;
        }

        d3_strobe = new ArrayList<Integer>(tmp);
//        System.out.println("d3_strobe "+d3_strobe);
        int first = d3_strobe.get(0);
        int last = d3_strobe.get(1);
//        System.out.println("first: "+first+" last: "+last);

        s1 = new ArrayList<>(s1.subList(first, last+1));
        s2 = new ArrayList<>(s2.subList(first, last+1));
        s3 = new ArrayList<>(s3.subList(first, last+1));
        s4 = new ArrayList<>(s4.subList(first, last+1));
        s5 = new ArrayList<>(s5.subList(first, last+1));
        s6 = new ArrayList<>(s6.subList(first, last+1));
        s7 = new ArrayList<>(s7.subList(first, last+1));
        s8 = new ArrayList<>(s8.subList(first, last+1));
        s9 = new ArrayList<>(s9.subList(first, last+1));
        s10 = new ArrayList<>(s10.subList(first, last+1));

        d1 = new ArrayList<>(d1.subList(first, last+1));
        d2 = new ArrayList<>(d2.subList(first, last+1));

        ArrayList<Integer> d1_strobe = new ArrayList<Integer>();
        ArrayList<ArrayList<Integer>> d1_map = new ArrayList<ArrayList<Integer>>();

        for (int i=0; i<d1.size()-1; i++)
        {
            if(d1.get(i+1) == 1 && d1.get(i) == 0)
            {
                d1_strobe.add(i);
            }
            if(d1.get(i+1) == 0 && d1.get(i) == 1)
            {
                d1_strobe.add(i+1);
                d1_map.add(d1_strobe);
                d1_strobe = new ArrayList<Integer>();
            }
        }

        ArrayList<ArrayList<Integer>> d1_map_temp = new ArrayList<ArrayList<Integer>>();
        for (int i=0; i<d1_map.size(); i++)
        {
            if (d1_map.get(i).size() == 2)
            {
                d1_map_temp.add(d1_map.get(i));
            }
        }
        d1_map = new ArrayList<>(d1_map_temp);

        ArrayList<ArrayList<Integer>> s1_map = new ArrayList<ArrayList<Integer>>();
        ArrayList<ArrayList<Integer>> s2_map = new ArrayList<ArrayList<Integer>>();
        ArrayList<ArrayList<Integer>> s3_map = new ArrayList<ArrayList<Integer>>();
        ArrayList<ArrayList<Integer>> s4_map = new ArrayList<ArrayList<Integer>>();
        ArrayList<ArrayList<Integer>> s5_map = new ArrayList<ArrayList<Integer>>();
        ArrayList<ArrayList<Integer>> s6_map = new ArrayList<ArrayList<Integer>>();
        ArrayList<ArrayList<Integer>> s7_map = new ArrayList<ArrayList<Integer>>();
        ArrayList<ArrayList<Integer>> s8_map = new ArrayList<ArrayList<Integer>>();
        ArrayList<ArrayList<Integer>> s9_map = new ArrayList<ArrayList<Integer>>();
        ArrayList<ArrayList<Integer>> s10_map = new ArrayList<ArrayList<Integer>>();

        s1_map = get_signal_map(s1);
        s2_map = get_signal_map(s2);
        s3_map = get_signal_map(s3);
        s4_map = get_signal_map(s4);
        s5_map = get_signal_map(s5);
        s6_map = get_signal_map(s6);
        s7_map = get_signal_map(s7);
        s8_map = get_signal_map(s8);
        s9_map = get_signal_map(s9);
        s10_map = get_signal_map(s10);

        ArrayList<Integer> d2_strobe_high = new ArrayList<>();
        ArrayList<Integer> d2_strobe_low = new ArrayList<>();
        ArrayList<ArrayList<Integer>> d2_map = new ArrayList<>();

        for (int i = 0; i<d2.size()-1; i++)
        {
            if(d2.get(i+1) == 0 && d2.get(i) == 1)
            {
                d2_strobe_high.add(i);
                d2_strobe_low.add(i+1);
                if(d2_strobe_high.size() == 2)
                {
                    d2_map.add(d2_strobe_high);
                    d2_strobe_high = new ArrayList<>();
                }
                else
                {
                    d2_strobe_high = new ArrayList<>();
                }
            }

            if(d2.get(i+1) == 1 && d2.get(i) == 0)
            {
                d2_strobe_high.add(i+1);
                d2_strobe_low.add(i);
                if(d2_strobe_low.size() == 2)
                {
                    d2_map.add(d2_strobe_low);
                    d2_strobe_low = new ArrayList<>();
                }
                else
                {
                    d2_strobe_low = new ArrayList<>();
                }
            }
        }

//        System.out.println("d2_map: "+d2_map);

        ArrayList<Integer> t = new ArrayList<>();
        int d2_map_last_value = d2_map.size()-1;
        t.add(d2_map.get(d2_map_last_value).get(1)+1);
        t.add(d3_strobe.get(1));
        d2_map.add(t);

        ArrayList<String> codes_s1 = new ArrayList<>();
        ArrayList<String> codes_s2 = new ArrayList<>();
        ArrayList<String> codes_s3 = new ArrayList<>();
        ArrayList<String> codes_s4 = new ArrayList<>();
        ArrayList<String> codes_s5 = new ArrayList<>();
        ArrayList<String> codes_s6 = new ArrayList<>();
        ArrayList<String> codes_s7 = new ArrayList<>();
        ArrayList<String> codes_s8 = new ArrayList<>();
        ArrayList<String> codes_s9 = new ArrayList<>();
        ArrayList<String> codes_s10 = new ArrayList<>();


        for (int i=0; i<d1_map.size(); i++)
        {
            String symbol_s1 = new String("");
            String symbol_s2 = new String("");
            String symbol_s3 = new String("");
            String symbol_s4 = new String("");
            String symbol_s5 = new String("");
            String symbol_s6 = new String("");
            String symbol_s7 = new String("");
            String symbol_s8 = new String("");
            String symbol_s9 = new String("");
            String symbol_s10 = new String("");

            int finish = d1_map.get(i).get(1);

            ArrayList<ArrayList<Integer>> temp_d2_map = new ArrayList<>();
            int d2_map_right_border = 0;
            for (int j=0; j<d2_map.size(); j++)
            {
                if (d2_map.get(j).get(0) <= finish && d2_map.get(j).get(1) >= finish)
                {
                    d2_map_right_border = j;
                }
            }

            temp_d2_map = new ArrayList<>(d2_map.subList(d2_map_right_border-7, d2_map_right_border+1));
//            System.out.println("d2: "+d2);
//            System.out.println("temp_d2_map: "+temp_d2_map);
            for (int k=0; k<temp_d2_map.size(); k++)
            {
                int d2_finish = temp_d2_map.get(k).get(1);
                int d2_start = temp_d2_map.get(k).get(0);

                symbol_s1 += get_bit_state(s1, s1_map, d2_start, d2_finish);
                symbol_s2 += get_bit_state(s2, s2_map, d2_start, d2_finish);
                symbol_s3 += get_bit_state(s3, s3_map, d2_start, d2_finish);
                symbol_s4 += get_bit_state(s4, s4_map, d2_start, d2_finish);
                symbol_s5 += get_bit_state(s5, s5_map, d2_start, d2_finish);
                symbol_s6 += get_bit_state(s6, s6_map, d2_start, d2_finish);
                symbol_s7 += get_bit_state(s7, s7_map, d2_start, d2_finish);
                symbol_s8 += get_bit_state(s8, s8_map, d2_start, d2_finish);
                symbol_s9 += get_bit_state(s9, s9_map, d2_start, d2_finish);
                symbol_s10 += get_bit_state(s10, s10_map, d2_start, d2_finish);

            }

            codes_s1.add(symbol_s1);
            codes_s2.add(symbol_s2);
            codes_s3.add(symbol_s3);
            codes_s4.add(symbol_s4);
            codes_s5.add(symbol_s5);
            codes_s6.add(symbol_s6);
            codes_s7.add(symbol_s7);
            codes_s8.add(symbol_s8);
            codes_s9.add(symbol_s9);
            codes_s10.add(symbol_s10);
        }


//        System.out.println("s1: "+s1);

        String result = new String("");
        String result_1 = new String("");
        String result_2 = new String("");
        String result_3 = new String("");
        String result_4 = new String("");
        String result_5 = new String("");
        String result_6 = new String("");
        String result_7 = new String("");
        String result_8 = new String("");
        String result_9 = new String("");
        String result_10 = new String("");

        result_1 = convert_code_to_text(codes_s1);
        result_2 = convert_code_to_text(codes_s2);
        result_3 = convert_code_to_text(codes_s3);
        result_4 = convert_code_to_text(codes_s4);
        result_5 = convert_code_to_text(codes_s5);
        result_6 = convert_code_to_text(codes_s6);
        result_7 = convert_code_to_text(codes_s7);
        result_8 = convert_code_to_text(codes_s8);
        result_9 = convert_code_to_text(codes_s9);
        result_10 = convert_code_to_text(codes_s10);

        result = result_1+result_2+result_3+result_4+result_5+result_6+result_7+result_8+result_9+result_10;

        return result;
    }

    static Map<String,Integer> prepare_sector(double pix_to_mm_ratio, int x0, int y0, int shift_x, int shift_y, int width, int height) {
        int deltaX = (int) (shift_x * pix_to_mm_ratio);

        int deltaY = (int) (shift_y * pix_to_mm_ratio);
        int w = (int) (width * pix_to_mm_ratio);
        int h = (int) (height * pix_to_mm_ratio);

        Map<String,Integer> sector = new HashMap<>();
        sector.put("x", x0+deltaX);
        sector.put("w", w);
        sector.put("y", y0+deltaY);
        sector.put("h", h);

        return sector;
    }

    static Mat get_image_independent_sector(Mat input_image, Map<String,Integer> sector) {
        /*
        Метод возвращает сектор (словарь) картинки
        Пример как сделать словарь: Map<String,Integer> main_sector = new HashMap<String,Integer>();
        */

        Mat output_image = new Mat();
        Integer y0 = sector.get("y");
        Integer y1 = y0+sector.get("h");
        Integer x0 = sector.get("x");
        Integer x1 = x0+sector.get("w");
//        Mat printimg = new Mat();
//        printimg = input_image.clone();
//        Size sz = new Size(input_image.size().width*2,input_image.size().height*2);
//        Imgproc.resize(input_image.clone(), printimg, sz);
//        Imgproc.rectangle(printimg, new Point(x0*2, y0*2), new Point(x1*2, y1*2),new Scalar(255, 0, 0, 255), 2);
//        HighGui.imshow("Image", printimg);
//        HighGui.waitKey(30);
//        System.out.println("input_image:"+input_image.size()+" x0: "+x0+" y0: "+y0+" x1: "+x1+" y1: "+y1);

        output_image = input_image.submat(y0, y1, x0, x1);

        return output_image;
    }

    static ArrayList<ArrayList<Integer>> get_color_map(Mat input_image) {
        /*
        Метод возвращает карту цветов картинки, привязанную к координатам пикселей
        */
        ArrayList<ArrayList<Integer>> color_map = new ArrayList<>();
        Size image_sz = input_image.size();
        byte buff[] = new byte[(int) (input_image.total() * input_image.channels())];

        int a_size = (int) image_sz.width;
        int b_size = (int) image_sz.height;

        double picdata[][] =  new double[a_size][b_size] ;
        double temp[];

//        List<Double> temp = new ArrayList<>();
        ArrayList<Integer> num = new ArrayList<>();

        for (int a=0; a<a_size; a++)
        {
            for (int b=0; b<b_size; b++)
            {
                temp = input_image.get(b, a);
                picdata[a][b] = (int) temp[0];

                num.clear();
                num.add(0,(int) picdata[a][b]);
                num.add(1, b);
                num.add(2, a);
                color_map.add(new ArrayList<Integer>(num));
//                System.out.println("max_Y:   "+color_map.get(a+b));
            }
        }

        return color_map;

    }

    static ArrayList<Point> get_brightest_spot_from_image(Mat image, int rad, int eq_bright_level, int alpha, int beta) {
        /*
        Метод возвращает координаты самой яркой области на картинке
        */
        ArrayList<ArrayList<Integer>> brightest_spots = new ArrayList<>();
        ArrayList<ArrayList<Integer>> brightest_spots_lengths_map = new ArrayList<>();
        ArrayList<ArrayList<Integer>> temp_for_map = new ArrayList<>();
        ArrayList<Integer> temp = new ArrayList<>();
        ArrayList<Point> circles_coordinates = new ArrayList<>();
        Mat img = new Mat();


        img = image.clone();

        double temp_length = 0;

//        int number_of_noise_bright_points = 5;
//        int number_of_base_points = 3;
        int total_number_of_points_to_search = 25;
        Mat gray = new Mat();

        Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);

//        Integer alpha = 3; // Contrast control (1.0-3.0)
//        Integer beta = 60; // Brightness control (0-100)
        Core.convertScaleAbs(gray.clone(), gray, alpha,  beta);

        Imgproc.GaussianBlur(gray.clone(), gray, new Size(5, 5), 3, 3);
//        HighGui.imshow("Image", gray);
//        HighGui.waitKey(500);


        ArrayList<Integer> maxval = new ArrayList<>();
        int maxval_temp = 0;

        for (int i=0; i<total_number_of_points_to_search; i++)
        {
//            Mat element = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE, new Size(2 * 3 + 1, 2 * 3 + 1),
//                    new Point(2, 2));
//            Imgproc.erode(gray.clone(), gray, element);
//            int x0 = (int) Core.minMaxLoc(gray).maxLoc.x;
//            int y0 = (int) Core.minMaxLoc(gray).maxLoc.y;

            Point bright_point = new Point();
            bright_point = Core.minMaxLoc(gray).maxLoc.clone();

            maxval_temp = (int) Core.minMaxLoc(gray).maxVal;

            if (maxval.size()>0)
            {
                if (maxval_temp*100/maxval.get(0) > eq_bright_level)
                {
                    maxval.add(maxval_temp);
                    circles_coordinates.add(bright_point);

                    Scalar color = new Scalar(0, 123, 123, 0);
                    Point center = new Point(bright_point.x, bright_point.y);
                    Imgproc.circle(gray, center, rad, color,-1);
//                    HighGui.imshow("Image", gray);
//                    HighGui.waitKey(10);
                }

            }
            else
            {
                maxval.add(maxval_temp);
            }
//            temp.add(x0);
//            temp.add(y0);
//            brightest_spots.add(new ArrayList<>(temp));
//            temp.clear();
        }
//        HighGui.imshow("Image", gray);
//        HighGui.waitKey(10);

        return circles_coordinates;
    }

    static Mat get_hsv_mask(Mat input_image, Scalar s1, Scalar s2)
    {
        Mat hsv = new Mat();

        Imgproc.cvtColor(input_image, hsv, Imgproc.COLOR_BGR2HSV);

        Core.inRange(hsv, s1, s2, hsv);
        input_image.copyTo(hsv, hsv);

//        HighGui.imshow("hsv", hsv.clone());
//        HighGui.waitKey(1);
        //hue range is [0,179], saturation range is [0,255], and value range is [0,255].
        return hsv;
    }

    static ArrayList<Mat> get_absdiff_video(ArrayList<Mat> video, Scalar s1, Scalar s2) {
        ArrayList<Mat> output_video = new ArrayList<>();
        Mat temp = new Mat();
//        Scalar s1 = new Scalar(55, 85, 100);


        for (int i=0; i<video.size()-1;i++)
        {
//            Core.absdiff(video.get(i), video.get(i+1), temp);

            temp = get_hsv_mask(video.get(i), s1, s2);
            output_video.add(temp.clone());
        }

        return output_video;
    }

    static ArrayList<Point> find_circles_from_image(Mat image) {
        /*
        Метод возвращает координаты найденных окружностей
        */
        ArrayList<Point> circles_coordinates = new ArrayList<>();


        Mat img = new Mat();
        img = image.clone();

        Mat circles = new Mat();

        Mat gray = new Mat();
        Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);

        Imgproc.HoughCircles(gray, circles, Imgproc.HOUGH_GRADIENT, 1.0,
                (double) 2, // change this value to detect circles with different distances to each other
                200, 3, 0, 4); // change the last two parameters
        // (min_radius & max_radius) to detect larger circles
        for (int x = 0; x < circles.cols(); x++) {
            double[] c = circles.get(0, x);
            Point center = new Point(Math.round(c[0]), Math.round(c[1]));
            Point temp = new Point();
            temp.x = c[0];
            temp.y = c[1];
            circles_coordinates.add(temp);

            // circle center
            Imgproc.circle(gray, center, 1, new Scalar(255,155,255), 2, -1, 0 );
            // circle outline
            int radius = (int) Math.round(c[2]);
//                System.out.println(radius);
            Imgproc.circle(img, center, radius+2, new Scalar(255,155,255), 2, -1, 0 );
        }
//        HighGui.imshow("detected circles", img);
//        HighGui.waitKey(50);
        System.out.println(circles_coordinates);
        return circles_coordinates;
    }

    static Point get_average_center(ArrayList<Point> circles_coordinates) {
        Point center = new Point(1, 1);
        int temp_x = 0;
        int temp_y = 0;
        try
        {
            for (int i=0; i<circles_coordinates.size(); i++)
            {
                temp_x += circles_coordinates.get(i).x;
                temp_y += circles_coordinates.get(i).y;
            }

            temp_x = (int) temp_x / circles_coordinates.size();
            temp_y = (int) temp_y / circles_coordinates.size();

            center.x = temp_x;
            center.y = temp_y;

        }
        catch (Exception e)
        {
            System.out.println("ебаная хуйня блять");
        }

        return center;
    }

    static ArrayList<Integer> get_border_blinks(ArrayList<Point> circles_coordinates)
    {
        ArrayList<Integer> result = new ArrayList<>();
        int temp_x_max = 0;
        int temp_x_min = 999999;
        int temp_y_max = 0;
        int temp_y_min = 999999;

        int cx = 0;
        int cy = 0;
        try
        {
            for (int i=0; i<circles_coordinates.size(); i++)
            {
                cx = (int) circles_coordinates.get(i).x;
                cy = (int) circles_coordinates.get(i).y;

                if (cx > temp_x_max){temp_x_max = (int) cx;}
                if (cx < temp_x_min){temp_x_min = (int) cx;}
                if (cy > temp_y_max){temp_y_max = (int) cy;}
                if (cy < temp_y_min){temp_y_min = (int) cy;}
            }
            int radius = (int) 0+(temp_y_max - temp_y_min)/2;
            result.add(temp_y_min-radius);
            result.add(temp_y_max+radius);
            result.add(temp_x_min-radius);
            result.add(temp_x_max+radius);
        }
        catch (Exception e)
        {
            System.out.println("Исключение в #get_border_blinks");
        }

        return result;
    }

    static Mat get_circles_from_image(Mat image)
    {

        Mat img = new Mat();
        Mat new_img = new Mat();
        Mat result = new Mat();
        img = image.clone();

        int total_number_of_points_to_search = 25;
        Mat gray = new Mat();

        Imgproc.cvtColor(img.clone(), gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(gray.clone(), gray, new Size(5, 5), 3, 3);
//        HighGui.imshow("Image", gray);
//        HighGui.waitKey(500);

        Point bright_point = new Point();
        bright_point = Core.minMaxLoc(gray).maxLoc.clone();


        int radius = 2;
        int y_center = (int) bright_point.x;
        int x_center = (int) bright_point.x;
//                System.out.println(radius);

//                Imgproc.circle(new_img, center, radius + 2, new Scalar(255, 155, 255), 2, -1, 0);
        result = img.submat(y_center-radius, y_center+radius, x_center-radius, x_center+radius);

//        Imgproc.resize(result, result, result.size(), 2,2);

//        HighGui.imshow("detected circles", img);
//        HighGui.waitKey(300);

        return result;
    }

    static ArrayList<Scalar> get_hsv_estimation(ArrayList<Mat> video)
    {
        ArrayList<Scalar> result = new ArrayList<>();
        ArrayList<Integer> average_hsv_img = new ArrayList<>();

        ArrayList<Integer> hue= new ArrayList<>();
        ArrayList<Integer> sat= new ArrayList<>();
        ArrayList<Integer> val= new ArrayList<>();

        for (int i=0; i < video.size(); i++)
        {
            Mat central_circle = new Mat();
            Mat current_img = new Mat();
            Imgproc.cvtColor(video.get(i), current_img, Imgproc.COLOR_BGR2HSV);
            central_circle = get_circles_from_image(current_img);

            if (!central_circle.empty())
            {
//                HighGui.imshow("detected circles", central_circle);
//                HighGui.waitKey(500);

                average_hsv_img = get_average_hsv_data(central_circle);

                if (average_hsv_img.get(2) != 0.0)
                {
                    hue.add(average_hsv_img.get(0));
                    sat.add(average_hsv_img.get(1));
                    val.add(average_hsv_img.get(2));
                }
            }
        }

        Collections.sort(hue);
        Collections.sort(sat);
        Collections.sort(val);

        int Hue_low = hue.get(0);
        int Sat_low = sat.get(0);
        int Val_low = val.get(0);
        int Hue_high = hue.get(hue.size()-1);
        int Sat_high = sat.get(sat.size()-1);
        int Val_high = val.get(val.size()-1);
        Scalar s1 = new Scalar(Hue_low, 50, Val_high-25);
        Scalar s2 = new Scalar(Hue_low+30, 113, Val_high);

        result.add(s1);
        result.add(s2);
        return result;
    }

    static ArrayList<Integer> get_average_hsv_data(Mat hsv)
    {
        int xc = (int) hsv.width() / 2;
        int yc = (int) hsv.height() / 2;
        ArrayList<Integer> result = new ArrayList<>();

        // System.out.print(" hsv: HUE "+hsv.get(xc, yc)[0]);
        // System.out.print("  SAT "+hsv.get(xc, yc)[1]);
        // System.out.println("  VALUE "+hsv.get(xc, yc)[2]);

//        System.out.print("  Mean Hue: " + Core.mean(hsv).val[0]);
//        System.out.print("  Sat: " + Core.mean(hsv).val[1]);
//        System.out.println("  Val: " + Core.mean(hsv).val[2]);

        result.add(Integer.valueOf((int) Core.mean(hsv).val[0]));
        result.add(Integer.valueOf((int) Core.mean(hsv).val[1]));
        result.add(Integer.valueOf((int) Core.mean(hsv).val[2]));

//        result.add(Integer.valueOf((int) hsv.get(xc, yc)[0]));
//        result.add(Integer.valueOf((int) hsv.get(xc, yc)[1]));
//        result.add(Integer.valueOf((int) hsv.get(xc, yc)[2]));


        System.out.print("  Mean Sat: "+Core.mean(hsv).val[1]);
        System.out.println("  Mean Val: "+Core.mean(hsv).val[2]);
        return result;
    }

    static ArrayList<Map<String,Integer>> get_color_map_white_borders(ArrayList<ArrayList<Integer>> color_map) {
        /*
        Метод возвращает координаты краевых точек
        */
        ArrayList<Map<String,Integer>> result = new ArrayList<>();
        ArrayList<Integer> x_list = new ArrayList<Integer>();
        ArrayList<Integer> y_list = new ArrayList<Integer>();
        Map<String,Integer> left = new HashMap<String,Integer>();
        Map<String,Integer> right = new HashMap<String,Integer>();

        for (int i=0; i<color_map.size(); i++)
        {
            if (color_map.get(i).get(0) != 0)
            {
                x_list.add(color_map.get(i).get(2));
                y_list.add(color_map.get(i).get(1));
            }
        }

        Integer x_min = Collections.min(x_list);
        Integer x_min_index = (int) x_list.indexOf(x_min);
        Integer y_min = y_list.get(x_min_index);

        Integer x_max = Collections.max(x_list);
        Integer x_max_index = (int) x_list.indexOf(x_min);
        Integer y_max = y_list.get(x_max_index);

        left.put("x", x_min);
        left.put("y", y_min);

        right.put("x", x_max);
        right.put("y", y_max);

        result.add(left);
        result.add(right);

        return result;
    }


    static Integer get_white_value(ArrayList<ArrayList<Integer>> color_map) {
        /*
        Метод возвращает кол-во белых пикселей в карте цветов
        */
        Integer white_value=0;

        for (int i=0; i<color_map.size(); i++)
        {
            Integer color = color_map.get(i).get(0);
            if (color == 255)
            {
                white_value ++;
            }
        }
        return white_value;
    }
    //
    static Integer get_status(Mat input_image) {
        /*
        Метод возвращает статус включения светодиодов
        */

        ArrayList<ArrayList<Integer>> color_map = new ArrayList<>(get_color_map(input_image));

        Integer white_value = get_white_value(color_map);

        if (white_value > 2)
        {
            return (int) 1;
        }
        else
        {
            return (int) 0;
        }
    }



    static ArrayList<Integer> get_image_status(Mat input_image, Scalar sc1, Scalar sc2, int rad, int eq_bright_level, int th_MinVal, int th_MaxVal, int alpha, int beta) {
        /*
        Метод возвращает результат анализа изображения
        */

        Mat big = new Mat();
        Mat hsv = new Mat();
        Mat gray = new Mat();
        Mat blurred = new Mat();
        Mat black_and_white = new Mat();
        Mat thresh = new Mat();


        hsv = get_hsv_mask(input_image, sc1, sc2);


//        Imgproc.cvtColor(hsv, hsv, Imgproc.COLOR_HSV2BGR);
        Imgproc.resize(hsv, big, input_image.size(), 3,3);
        ArrayList<Point> circles_coordinates = get_brightest_spot_from_image(big, rad, eq_bright_level, alpha, beta);



        Imgproc.cvtColor(big, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(gray, blurred, new Size(11, 11), 0);

        Core.convertScaleAbs(blurred, black_and_white, alpha,  beta);
        Imgproc.threshold(black_and_white, thresh, th_MinVal, th_MaxVal, Imgproc.THRESH_BINARY);




//        HighGui.imshow("black_and_white", black_and_white);
//        HighGui.waitKey(100);




//        int xR = coordinates.get(1).get("x");
//        int yR = coordinates.get(1).get("y");
//        int xL = coordinates.get(0).get("x");
//        int yL = coordinates.get(0).get("y");

        int xR = (int) circles_coordinates.get(0).x;
        int yR = (int) circles_coordinates.get(0).y;
        int xL = (int) circles_coordinates.get(0).x;
        int yL = (int) circles_coordinates.get(0).y;

        for (int kk=0; kk<circles_coordinates.size(); kk++)
        {
            Point buffer_point = new Point();
            buffer_point = circles_coordinates.get(kk);
//            System.out.println(buffer_point);
            if (xL > (int) buffer_point.x)
            {
                xL = (int) buffer_point.x;
                yL = (int) buffer_point.y;
            }

            if (xR < (int) buffer_point.x)
            {
                xR = (int) buffer_point.x;
                yR = (int) buffer_point.y;
            }
        }


//        System.out.println("R: "+xR+" "+yR);
//        System.out.println("L: "+xL+" "+yL);

//        Mat printimg = new Mat();
//        printimg = hsv.clone();
//        Imgproc.rectangle(printimg, new Point(xR, yR), new Point(xL,  yL),new Scalar(255, 255, 255, 255), 2);
////        cv2.rectangle(print_image, (x0 + deltaX, y0 + deltaY), (x0 + deltaX + w, y0 + deltaY + h), (255, 255, 255), 2)
//        HighGui.imshow("Image", printimg);
//        HighGui.waitKey(30);

        int Lmax = Math.abs(xR - xL);

        double pix_to_mm_ratio =  (double) Lmax / (double) 195;

        Mat s1 = new Mat();
        Mat s2 = new Mat();
        Mat s3 = new Mat();
        Mat s4 = new Mat();
        Mat s5 = new Mat();
        Mat s6 = new Mat();
        Mat s7 = new Mat();
        Mat s8 = new Mat();
        Mat s9 = new Mat();
        Mat s10 = new Mat();

        Mat d1 = new Mat();
        Mat d2 = new Mat();
        Mat d3 = new Mat();

        Map<String,Integer> sector = new HashMap<String,Integer>();
        sector = prepare_sector(pix_to_mm_ratio, xL, yL, 86-1, -35-3, 10, 10);
        s1 = get_image_independent_sector(thresh, sector);

        sector = new HashMap<>(prepare_sector(pix_to_mm_ratio, xL, yL, 109, -35-3, 10, 10));
        s2 = get_image_independent_sector(thresh, sector);

        sector = new HashMap<>(prepare_sector(pix_to_mm_ratio, xL, yL, 37, 0-2, 10, 10));
        s3 = get_image_independent_sector(thresh, sector);

        sector = new HashMap<>(prepare_sector(pix_to_mm_ratio, xL, yL, 74, 0-2, 10, 10));
        s4 = get_image_independent_sector(thresh, sector);

        sector = new HashMap<>(prepare_sector(pix_to_mm_ratio, xL, yL, 120, 0-2, 10, 10));
        s5 = get_image_independent_sector(thresh, sector);

        sector = new HashMap<>(prepare_sector(pix_to_mm_ratio, xL, yL, 158, 0-2, 10, 10));
        s6 = get_image_independent_sector(thresh, sector);

        sector = new HashMap<>(prepare_sector(pix_to_mm_ratio, xL, yL, 30, 22-2, 10, 10));
        s7 = get_image_independent_sector(thresh, sector);

        sector = new HashMap<>(prepare_sector(pix_to_mm_ratio, xL, yL, 165, 22-2, 10, 10));
        s8 = get_image_independent_sector(thresh, sector);


        sector = new HashMap<>(prepare_sector(pix_to_mm_ratio, xL, yL, 60, 44-2, 10, 10));
        s9 = get_image_independent_sector(thresh, sector);


        sector = new HashMap<>(prepare_sector(pix_to_mm_ratio, xL, yL, 135, 44-2, 10, 10));
        s10 = get_image_independent_sector(thresh, sector);

        sector = new HashMap<>(prepare_sector(pix_to_mm_ratio, xL, yL, 97, 71-2, 10, 10));
        d2 = get_image_independent_sector(thresh, sector);

        sector = new HashMap<>(prepare_sector(pix_to_mm_ratio, xL, yL, 49, 79-2, 10, 10));
        d1 = get_image_independent_sector(thresh, sector);

        sector = new HashMap<>(prepare_sector(pix_to_mm_ratio, xL, yL, 146, 79-2, 10, 10));
        d3 = get_image_independent_sector(thresh, sector);

        ArrayList<Integer> status = new ArrayList<Integer>();

        status.add(get_status(s1));
        status.add(get_status(s2));
        status.add(get_status(s3));
        status.add(get_status(s4));
        status.add(get_status(s5));
        status.add(get_status(s6));
        status.add(get_status(s7));
        status.add(get_status(s8));
        status.add(get_status(s9));
        status.add(get_status(s10));

        status.add(get_status(d1));
        status.add(get_status(d2));
        status.add(get_status(d3));

        System.out.println("Анализ текущего кадра: "+status);
        return status;
    }

    static Mat test_get_thresh(Mat input_image) {
        /*
        Метод возвращает результат анализа изображения
        */

        Mat big = new Mat();
        Mat gray = new Mat();
        Mat blurred = new Mat();
        Mat black_and_white = new Mat();
        Mat thresh = new Mat();

        Imgproc.resize(input_image, big, input_image.size(), 2,2);
        Imgproc.cvtColor(big, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(gray, blurred, new Size(11, 11), 0);
        Integer alpha = 2; // Contrast control (1.0-3.0)
        Integer beta = 50; // Brightness control (0-100)
        Core.convertScaleAbs(blurred, black_and_white, alpha,  beta);
        Imgproc.threshold(black_and_white, thresh, 200, 255, Imgproc.THRESH_BINARY);
//        HighGui.imshow("Image", thresh);
//        HighGui.waitKey(30);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(thresh, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
//        hierarchy.release();

        for ( int i=0; i<contours.size(); i++ )
        {
            MatOfPoint2f approxCurve = new MatOfPoint2f();
            MatOfPoint2f contour2f = new MatOfPoint2f(contours.get(i).toArray());
            double approxDistance = Imgproc.arcLength(contour2f,  true)*0.02;
            Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);

            MatOfPoint points = new MatOfPoint( approxCurve.toArray() );
            // Get bounding rect of contour
            Rect rect = Imgproc.boundingRect(points);

            if (rect.height != 0)
            {
//                System.out.println("-кок кок-");
                System.out.println((float) rect.width / rect.height);
                if ((float) rect.width / rect.height > 1.4 && (float) rect.width / rect.height < 1.6 && rect.width > 80 && rect.height > 80) {
//                    System.out.println(rect.width);
//                    System.out.println(rect.height);
//                    System.out.println(rect.x);
//                    System.out.println(rect.y);
                    Integer delta_Y = 10;
                    Integer delta_X = 50;

                    Map<String,Integer> main_sector = new HashMap<String,Integer>();
                    main_sector.put("x", rect.x+delta_X);
                    main_sector.put("w", rect.x+ rect.width-delta_X);
                    main_sector.put("y", rect.y+delta_Y);
                    main_sector.put("h", rect.y+ rect.height-delta_Y);


                    thresh = get_image_independent_sector(thresh, main_sector);
                }
            }



        }

        return thresh;
    }

    static ArrayList<Point> extend_point_list(ArrayList<Point> src1, ArrayList<Point> src2) {
        ArrayList<Point> output = new ArrayList<>();
        int radius = 10;
        boolean permission = true;

        for (int i=0; i<src1.size();i++)
        {
            permission = true;
            for (int j=0; j<src2.size();j++)
            {
                if ((src2.get(j).x < src1.get(i).x+radius) && (src2.get(j).x > src1.get(i).x-radius)&& (src2.get(j).y > src1.get(i).y-radius)&& (src2.get(j).y < src1.get(i).y+radius))
                {
//                    System.out.println("Совпадение бля");
                    permission = false;
                }

            }

            if (permission)
            {
                src2.add(src1.get(i));
            }

        }
        return src2;
    }



    static ArrayList<ArrayList<Integer>> get_start_stop(int video_size, int threads_number)
    {
        int length = (int) video_size/threads_number;
        int delta = video_size-length*threads_number;
        ArrayList<ArrayList<Integer>> result = new ArrayList<>();
        ArrayList<Integer> pair = new ArrayList<>();

        for (int i=0; i<threads_number; i++)
        {
            pair.add(i*length+1);
            if (i == threads_number-1)
            {
                pair.add((i+1)*length+delta-1);
            }
            else
            {
                pair.add((i+1)*length);
            }

            result.add(pair);
            pair = new ArrayList<>();
        }

        return result;
    }

    static ArrayList<ArrayList<Double>> unite_df(ArrayList<ArrayList<ArrayList<Double>>> df_list)
    {
        ArrayList<Double> df_row = new ArrayList<Double>();
        ArrayList<ArrayList<Double>> total = new ArrayList<>();

        for (int k=0; k<df_list.get(0).size(); k++)
        {
            for (int i=0; i<df_list.size(); i++)
            {
                for (int j=0; j<df_list.get(0).get(0).size(); j++)
                {
                    df_row.add(df_list.get(i).get(k).get(j));
                }


            }

            total.add(df_row);
            df_row = new ArrayList<>();

        }

        return total;
    }

    static ArrayList<ArrayList<Double>> mainly(ArrayList<Mat> video, int start, int finish, Scalar s1_test, Scalar s2, int radius_for_bright_points_search, int eq_bright_level, int th_MinVal, int th_MaxVal, int alpha, int beta) {
        /*
        Метод возвращает анализ видео
        */
        ArrayList<ArrayList<Integer>> array_start_stop = new ArrayList<>(get_start_stop(video.size(), 5));

        ImplementMyRunnable object1 = new ImplementMyRunnable();
        object1.setParams(video,array_start_stop.get(0).get(0),array_start_stop.get(0).get(1),s1_test, s2, radius_for_bright_points_search, eq_bright_level, th_MinVal, th_MaxVal, alpha, beta);
        Thread myThread1 = new Thread(object1);

        ImplementMyRunnable object2 = new ImplementMyRunnable();
        object2.setParams(video,array_start_stop.get(1).get(0),array_start_stop.get(1).get(1),s1_test, s2, radius_for_bright_points_search, eq_bright_level, th_MinVal, th_MaxVal, alpha, beta);
        Thread myThread2 = new Thread(object2);

        ImplementMyRunnable object3 = new ImplementMyRunnable();
        object3.setParams(video,array_start_stop.get(2).get(0),array_start_stop.get(2).get(1),s1_test, s2, radius_for_bright_points_search, eq_bright_level, th_MinVal, th_MaxVal, alpha, beta);
        Thread myThread3 = new Thread(object3);

        ImplementMyRunnable object4 = new ImplementMyRunnable();
        object4.setParams(video,array_start_stop.get(3).get(0),array_start_stop.get(3).get(1),s1_test, s2, radius_for_bright_points_search, eq_bright_level, th_MinVal, th_MaxVal, alpha, beta);
        Thread myThread4 = new Thread(object4);

        ImplementMyRunnable object5 = new ImplementMyRunnable();
        object5.setParams(video,array_start_stop.get(4).get(0),array_start_stop.get(4).get(1),s1_test, s2, radius_for_bright_points_search, eq_bright_level, th_MinVal, th_MaxVal, alpha, beta);
        Thread myThread5 = new Thread(object5);

        myThread1.start();
        myThread2.start();
        myThread3.start();
        myThread4.start();
        myThread5.start();

        try {
            myThread1.join();
            myThread2.join();
            myThread3.join();
            myThread4.join();
            myThread5.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println(e);
            System.out.println("error threads");
        }

        ArrayList<ArrayList<ArrayList<Double>>> df_list = new ArrayList<>();

        df_list.add(object1.getValue());
        df_list.add(object2.getValue());
        df_list.add(object3.getValue());
        df_list.add(object4.getValue());
        df_list.add(object5.getValue());

        /*System.out.println(df_list);
        System.out.println(array_start_stop);
        System.out.println(video.size());*/

        return unite_df(df_list);
    }

    static Mat get_rotate(Mat src, double angle) {
        Point center = new Point(src.width() / 2, src.height() / 2);
        Mat rotImage = Imgproc.getRotationMatrix2D(center, angle, 1.0);
        Size size = new Size(src.width(), src.height());
        Mat imageOutput = new Mat();
        Imgproc.warpAffine(src, imageOutput, rotImage, size, Imgproc.INTER_LINEAR
                + Imgproc.CV_WARP_FILL_OUTLIERS);
        return imageOutput;
    }

    static double mean(ArrayList<Integer> marks) {
        Integer sum = 0;
        if(!marks.isEmpty()) {
            for (Integer mark : marks) {
                sum += mark;
            }
            return sum.doubleValue() / marks.size();
        }
        return sum;
    }

    static double sd (ArrayList<Integer> table)
    {
        double mean = mean(table);
        double temp = 0;
        for (int i = 0; i < table.size(); i++)
        {
            int val = table.get(i);
            double squrDiffToMean = Math.pow(val - mean, 2);
            temp += squrDiffToMean;
        }
        double meanOfDiffs = (double) temp / (double) (table.size());
        return Math.sqrt(meanOfDiffs);
    }

    static Scalar find_scalars_2(ArrayList<Mat> video, int rad, int eq_bright_level, int alpha, int beta, Scalar s1) {
        System.out.println("Автоопределение HSV порогов..");

        double pos_sd_level = 0.7;
        Mat big = new Mat();
        Mat hsv = new Mat();
        Scalar result = new Scalar(0, 40, 160);
        result = s1;

        boolean search_status = false;

        Scalar sc2 = new Scalar(178, 255, 255);
        for (int i = 140; i < 240; i += 20) //value
        {
            for (int j = 20; j < 110; j += 10) //saturation
            {

                System.out.println("Яркость: " + i + "; Насыщенность: " + j);
                Scalar sc1 = new Scalar(0, j, i);
                ArrayList<Integer> xr_list = new ArrayList<>();
                ArrayList<Integer> yr_list = new ArrayList<>();
                for (int k = 0; k < video.size(); k += 4) {
                    Mat temp_pic = new Mat();
                    temp_pic = video.get(k);
                    hsv = get_hsv_mask(temp_pic, sc1, sc2);
                    Imgproc.resize(hsv, big, temp_pic.size(), 3, 3);
                    ArrayList<Point> circles_coordinates = get_brightest_spot_from_image(big, rad, eq_bright_level, alpha, beta);

                    int xR = (int) circles_coordinates.get(0).x;
                    int yR = (int) circles_coordinates.get(0).y;
                    int xL = (int) circles_coordinates.get(0).x;
                    int yL = (int) circles_coordinates.get(0).y;

                    for (int kk = 0; kk < circles_coordinates.size(); kk++) {
                        Point buffer_point = new Point();
                        buffer_point = circles_coordinates.get(kk);
                        if (xL > (int) buffer_point.x) {
                            xL = (int) buffer_point.x;
                            yL = (int) buffer_point.y;
                        }

                        if (xR < (int) buffer_point.x) {
                            xR = (int) buffer_point.x;
                            yR = (int) buffer_point.y;
                        }
                    }
                    xr_list.add(Math.abs(xR - xL));
                    yr_list.add(Math.abs(yR - yL));

                    if (xr_list.size() > 1 && yr_list.size() > 1) {
                        double temp_summ = 0;
                        temp_summ = sd(xr_list);
                        double temp_summ_yr = 0;
                        temp_summ_yr = sd(yr_list);

                        if (k > video.size() / 5 && temp_summ < pos_sd_level && temp_summ_yr < pos_sd_level)
                            /*0.6, 0.45*/ {
//                            System.out.print("xr_list: ");
//                            System.out.println(xr_list);
                            System.out.println("------------------------------------------");
                            System.out.print("Отклонение X: ");
                            System.out.println(temp_summ);
                            System.out.print("Отклонение Y: ");
                            System.out.println(temp_summ_yr);

//                            System.out.print("delta Y: ");
//                            System.out.println(Math.abs(yR-yL));
                            System.out.print("Выбранный порог яркости: ");
                            System.out.println(i);
                            System.out.print("Выбранный порог насыщенности: ");
                            System.out.println(j);
                            System.out.println("------------------------------------------");
                            Scalar buffer_scalar = new Scalar((int)s1.val[0], j, i);
                            result = buffer_scalar;
                            k = video.size();
                            i = 240;
                            j = 140;
                            search_status = true;
                        } else if (k > video.size() / 5) {
                            k = video.size();
                        }

                    }
                }
            }
        }

        if (search_status == false) {
            System.out.println("\nНе удалось найти пороги HSV!");
            System.out.println("Применены значения порогов по умолчанию: " + result);
            System.out.println("------------------------------------------");
        }

        System.out.println("Средняя яркость до изменения beta: " + Core.mean(video.get(0)).val[2]);
        Mat test_img = new Mat();
        Core.convertScaleAbs(video.get(0), test_img, 3, beta);
        System.out.println("Средняя яркость после изменения beta: " + Core.mean(test_img.clone()).val[2]);
        System.out.println("------------------------------------------");


        return result;
    }
}
