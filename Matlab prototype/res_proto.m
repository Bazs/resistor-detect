close all
clear all

% RGB color and covariance database:
% 1-black, 2-brown, 3-red, 4-orange, 5-yellow, 6-green
% 7-blue, 8-violet, 9-gray, 10-white, 11-gold, 12-silver
% The next section loads the precompiled pixel data into Matlab,
% then calculates the mean and the inverse of the covariance matrix
% for the Mahalanobis distance
load orange_pixeldata
orange_mu = mean(orange_pixeldata);
orange_Cinv = inv(cov(double(orange_pixeldata)));
load brown_pixeldata
brown_mu = mean(brown_pixeldata);
brown_Cinv = inv(cov(double(brown_pixeldata)));
load white_pixeldata
white_mu = mean(white_pixeldata);
white_Cinv = inv(cov(double(white_pixeldata)));
load red_pixeldata
red_mu = mean(red_pixeldata);
red_Cinv = inv(cov(double(red_pixeldata)));
load green_pixeldata
green_mu = mean(green_pixeldata);
green_Cinv = inv(cov(double(green_pixeldata)));
load yellow_pixeldata
yellow_mu = mean(yellow_pixeldata);
yellow_Cinv = inv(cov(double(yellow_pixeldata)));
load black_pixeldata
black_mu = mean(black_pixeldata);
black_Cinv = inv(cov(double(black_pixeldata)));
load violet_pixeldata
violet_mu = mean(violet_pixeldata);
violet_Cinv = inv(cov(double(violet_pixeldata)));
load blue_pixeldata
blue_mu = mean(blue_pixeldata);
blue_Cinv = inv(cov(double(blue_pixeldata)));
load gray_pixeldata
gray_mu = mean(gray_pixeldata);
gray_Cinv = inv(cov(double(gray_pixeldata)));
load gold_pixeldata
[Zs idx] = sort(gold_pixeldata(:,4));
gold_pixeldata = gold_pixeldata(idx,:);
gold_C = cov(double(gold_pixeldata(:,[1 2 3])));

colors_mu = [black_mu;brown_mu;red_mu;orange_mu;yellow_mu;green_mu;blue_mu; violet_mu;gray_mu;white_mu];

colortext = {'black' 'brown' 'red' 'orange' 'yellow' 'green' 'blue' 'violet' 'gray' 'white' 'gold' 'silver'};
tolerances =[0 1 2 0 0 0.5 0.25 0.1 0.05 0 5 10];

I = imread('resistors_10.jpg');
% Iprops = imfinfo('resistors_18.jpg');
% Isize = Iprops.FileSize;
% 
% str_base_size = round(3 * 2224155 / Isize);
% if (~str_base_size)
%     str_base_size = 1;
% end

str_base_size = 3;

str_bands = strel('disk',str_base_size,8);
str_d = strel('disk',str_base_size*4,8);
str_e = strel('disk',str_base_size*4,8);
str_back = strel('disk',str_base_size*7,8);

Ihsv = rgb2hsv(I);
Ig = rgb2gray(I);
Ih = Ihsv(:,:,1);
Is = Ihsv(:,:,2);
Iv = Ihsv(:,:,3);

Ith_e = edge(Ig,'sobel');
Ith_e = imdilate(Ith_e,str_d);
Ith_e = imdilate(Ith_e,str_d);
Ith_e = imdilate(Ith_e,str_d);
Ith_e = imerode(Ith_e,str_e);
Ith_e = imerode(Ith_e,str_e);
Ith_e = imerode(Ith_e,str_e);
Ith_s = im2bw(Is,0.3);
Ith_s = imdilate(Ith_s,str_d);
Ith_s = imerode(Ith_s,str_e);
Ith = Ith_e & Ith_s;
Ith_all = Ith;
Ith_all = imdilate(Ith_all,str_back); % strongly dilate to get total area of resistors

Ibuff = zeros(size(Ith,1),size(Ith,2),4);
runs = 0;
memory = 2; %% sets from how far back do we want to start the regrowth by dilatation


while (sum(sum(Ith==1))~=0),  
    Ibuff(:,:,4)=Ibuff(:,:,3);
    Ibuff(:,:,3)=Ibuff(:,:,2);
    Ibuff(:,:,2)=Ibuff(:,:,1);
    Ibuff(:,:,1)= Ith;
    Ith = imerode(Ith,str_e);
    runs = runs + 1;
    if (runs > 15),
        msgbox('Too many erosions.');
        return;
    end
end

Ith = Ibuff(:,:,memory);

for i=(1:(runs-memory)),
    Ith = imdilate(Ith,str_d);
end

[L, num] = bwlabel(Ith);
Res1m = L == 1; % select only one resistor
Ith = Ith & Res1m;

% get the bounding box of the resistor mask
box = regionprops(Ith,'BoundingBox');

% prepare an empty image the size of the resistor's bounding box
Ires = uint8(zeros(floor(box.BoundingBox(4))+1,floor(box.BoundingBox(3))+1,3));


% crop the whole image so that it shows only the resistor
Iresr = I(floor(box.BoundingBox(2)):(floor(box.BoundingBox(2))+floor(box.BoundingBox(4))), floor(box.BoundingBox(1)):(floor(box.BoundingBox(1))+floor(box.BoundingBox(3))),1);
Iresg = I(floor(box.BoundingBox(2)):(floor(box.BoundingBox(2))+floor(box.BoundingBox(4))), floor(box.BoundingBox(1)):(floor(box.BoundingBox(1))+floor(box.BoundingBox(3))),2);
Iresb = I(floor(box.BoundingBox(2)):(floor(box.BoundingBox(2))+floor(box.BoundingBox(4))), floor(box.BoundingBox(1)):(floor(box.BoundingBox(1))+floor(box.BoundingBox(3))),3);
Ires(:,:,1) = Iresr;
Ires(:,:,2) = Iresg;
Ires(:,:,3) = Iresb;

% this will be the background image
Iback = uint8(zeros(size(I,1),size(I,2),size(I,3)));

% crop the binary mask to show only the resistor
Ith = Ith(floor(box.BoundingBox(2)):(floor(box.BoundingBox(2))+floor(box.BoundingBox(4))), floor(box.BoundingBox(1)):(floor(box.BoundingBox(1))+floor(box.BoundingBox(3))));

% mask out the resistors to get the background
Iback(:,:,1) = I(:,:,1) .* uint8(~(Ith_all));
Iback(:,:,2) = I(:,:,2) .* uint8(~(Ith_all));
Iback(:,:,3) = I(:,:,3) .* uint8(~(Ith_all));

% remove the background from the resistor image
Ires(:,:,1) = Ires(:,:,1) .* uint8(Ith);
Ires(:,:,2) = Ires(:,:,2) .* uint8(Ith);
Ires(:,:,3) = Ires(:,:,3) .* uint8(Ith);



% color corrected resistor 
Iresc = Ires;

% calibrate the colors based on the assumption that the background is
% white, plus perform a 3by3 median filtering
back_R = sum(sum(Iback(:,:,1)));
back_G = sum(sum(Iback(:,:,2)));
back_B = sum(sum(Iback(:,:,3)));
back_max = max([back_R back_G back_B]);
% Iresc(:,:,1) = medfilt2(uint8(double(Ires(:,:,1)) * back_max/back_R), [3 3]);
% Iresc(:,:,2) = medfilt2(uint8(double(Ires(:,:,2)) * back_max/back_G), [3 3]);
% Iresc(:,:,3) = medfilt2(uint8(double(Ires(:,:,3)) * back_max/back_B), [3 3]);
num_back_pixels = 0;
back_pixels = 0;
Iresg = rgb2gray(Iresc);
for i=1:size(Iresc,1)
    for j=1:size(Iresc,2)
        if(sum(Iresc(i,j,:)))
            num_back_pixels = num_back_pixels + 1;
            back_pixels(num_back_pixels, [1 2 3]) = Iresc(i,j,:);
        end
        distance = sqrt((double(Iresc(i,j,1))-255)^2 + (double(Iresc(i,j,2))-255)^2 + (double(Iresc(i,j,3))-255)^2);
        if (Iresg(i,j) > 230)
            Iresc(i,j,:) = 0;
        end
    end
end

body_color = median(double(back_pixels(:,[1 2 3])));
body_color_min_dist = 256;
for i=1:10
    distance = sqrt((colors_mu(i,1)-body_color(1))^2 + (colors_mu(i,2)-body_color(2))^2 + (colors_mu(i,3)-body_color(3))^2);
    if (distance < body_color_min_dist),
        body_color_min_dist = distance;
    end
end

background_threshold = 1/body_color_min_dist*300;

if (background_threshold > 10)
    for i=1:size(Iresc,1)
        for j=1:size(Iresc,2)
            if(sum(Iresc(i,j,:)))
                distance = sqrt((double(Iresc(i,j,1))-body_color(1))^2 + (double(Iresc(i,j,2))-body_color(2))^2 + (double(Iresc(i,j,3))-body_color(3))^2);
                if (distance < background_threshold)
                    Iresc(i,j,:) = 0;
                end
            end
        end
    end
end


mask_props = regionprops(Ith,'Centroid','Orientation','MajorAxisLength','MinorAxisLength');
% determine the parameters line which runs throught the middle of the resistor
% orientation of the line
alpha = mask_props.Orientation*pi/180;
% orientation vector of the line
v = [1;tan(alpha)];
% matrix of orthogonal projection onto the line
P = v*v'/(v'*v);
% line equation parameters
A = tan(alpha);
B = -1;
C = mask_props.Centroid(2)-A*mask_props.Centroid(1);

Ibands = zeros(size(Iresc,1),size(Iresc,2),12);

for i=1:size(Ires,1),
    for j=1:size(Ires,2),
        % compute the Mahalanobis distance for orange
        distance = sqrt((double([Iresc(i,j,1) Iresc(i,j,2) Iresc(i,j,3)])-orange_mu)*orange_Cinv*(double([Iresc(i,j,1) Iresc(i,j,2) Iresc(i,j,3)])-orange_mu)');
        if (distance < 2 && sum(Iresc(i,j,:))), 
            Ibands(i,j,4) = 1;
        end
        % for brown
        distance = sqrt((double([Iresc(i,j,1) Iresc(i,j,2) Iresc(i,j,3)])-brown_mu)*brown_Cinv*(double([Iresc(i,j,1) Iresc(i,j,2) Iresc(i,j,3)])-brown_mu)');
        if (distance < 2 && sum(Iresc(i,j,:))), 
            Ibands(i,j,2) = 1;
        end
        % for white
        distance = sqrt((double([Iresc(i,j,1) Iresc(i,j,2) Iresc(i,j,3)])-white_mu)*white_Cinv*(double([Iresc(i,j,1) Iresc(i,j,2) Iresc(i,j,3)])-white_mu)');
        if (distance < 2 && sum(Iresc(i,j,:))), 
            Ibands(i,j,10) = 1;
        end
        % for red
        distance = sqrt((double([Iresc(i,j,1) Iresc(i,j,2) Iresc(i,j,3)])-red_mu)*red_Cinv*(double([Iresc(i,j,1) Iresc(i,j,2) Iresc(i,j,3)])-red_mu)');
        if (distance < 2 && sum(Iresc(i,j,:))), 
            Ibands(i,j,3) = 1;
        end
        % for green
        distance = sqrt((double([Iresc(i,j,1) Iresc(i,j,2) Iresc(i,j,3)])-green_mu)*green_Cinv*(double([Iresc(i,j,1) Iresc(i,j,2) Iresc(i,j,3)])-green_mu)');
        if (distance < 2 && sum(Iresc(i,j,:))), 
            Ibands(i,j,6) = 1;
        end
        % for yellow
        distance = sqrt((double([Iresc(i,j,1) Iresc(i,j,2) Iresc(i,j,3)])-yellow_mu)*yellow_Cinv*(double([Iresc(i,j,1) Iresc(i,j,2) Iresc(i,j,3)])-yellow_mu)');
        if (distance < 2 && sum(Iresc(i,j,:))), 
            Ibands(i,j,5) = 1;
        end
        % for black
        distance = sqrt((double([Iresc(i,j,1) Iresc(i,j,2) Iresc(i,j,3)])-black_mu)*black_Cinv*(double([Iresc(i,j,1) Iresc(i,j,2) Iresc(i,j,3)])-black_mu)');
        if (distance < 2 && sum(Iresc(i,j,:))), 
            Ibands(i,j,1) = 1;
        end
        % for violet
        distance = sqrt((double([Iresc(i,j,1) Iresc(i,j,2) Iresc(i,j,3)])-violet_mu)*violet_Cinv*(double([Iresc(i,j,1) Iresc(i,j,2) Iresc(i,j,3)])-violet_mu)');
        if (distance < 2 && sum(Iresc(i,j,:))), 
            Ibands(i,j,8) = 1;
        end
        % for blue
        distance = sqrt((double([Iresc(i,j,1) Iresc(i,j,2) Iresc(i,j,3)])-blue_mu)*blue_Cinv*(double([Iresc(i,j,1) Iresc(i,j,2) Iresc(i,j,3)])-blue_mu)');
        if (distance < 2 && sum(Iresc(i,j,:))), 
            Ibands(i,j,7) = 1;
        end
         % for gray
        distance = sqrt((double([Iresc(i,j,1) Iresc(i,j,2) Iresc(i,j,3)])-gray_mu)*gray_Cinv*(double([Iresc(i,j,1) Iresc(i,j,2) Iresc(i,j,3)])-gray_mu)');
        if (distance < 2 && sum(Iresc(i,j,:))), 
            Ibands(i,j,9) = 1;
        end
%         % for gold
%         d = round(double(abs(A*j+B*(size(Ith,1)-i)+C)/sqrt(A^2+B^2))/mask_props.MajorAxisLength*300.0);
%         if (d < size(gold_mu,1)),
%             distance = sqrt((double([Iresc(i,j,1) Iresc(i,j,2) Iresc(i,j,3)])-gold_mu(d+1,:))*[gold_Cinv(d+1,:,1);gold_Cinv(d+1,:,2);gold_Cinv(d+1,:,3)]*(double([Iresc(i,j,1) Iresc(i,j,2) Iresc(i,j,3)])-gold_mu(d+1,:))');
%             if (distance < 2 && sum(Ixyz(i,j,:))), 
%              Ibands(i,j,11) = 1;
%             end
%         end
    end
end

% first element will be the area of the band with the largest area
% second element is the index of the band
largest_area = [0 0];
% labeled regions in the bands
label = zeros(size(Ibands));
num = zeros(10,1);

colorprops = zeros(12,2);
for i=1:12,
    % morphological closing
    Ibands(:,:,i) = imdilate(Ibands(:,:,i),str_bands);
    Ibands(:,:,i) = imerode(Ibands(:,:,i),str_bands);
    % morphological opening
    Ibands(:,:,i) = imerode(Ibands(:,:,i),str_bands);
    Ibands(:,:,i) = imdilate(Ibands(:,:,i),str_bands);
    
    % determine which band has the largest connected region and how large
    % it is
    if (i<11),
        [label(:,:,i), num(i)] = bwlabel(Ibands(:,:,i),8);
        for j=1:num(i),
            if(size(find(label(:,:,i)==j),1) > largest_area(1)),
                largest_area(1) = size(find(label(:,:,i)==j),1);
                largest_area(2) = i;
            end
        end
    end
end



% will contain the number of found bands
numbands = 0;

% filter out the bands which have only connected regions with less than 35%
% of the largest connected region
for i=1:10,
    for j=1:num(i),
        if(size(find(label(:,:,i)==j),1) > largest_area(1)*0.2), 
            colorprops(i,1) = size(find(label(:,:,i)==j),1);
            colorprops(i,2) = colorprops(i,2) + 1;
            prp = regionprops(label(:,:,i) == j,'Centroid');
            bandprops(i,colorprops(i,2),[1 2]) = P*[prp.Centroid(1);size(Ith,1)-prp.Centroid(2)];
            numbands = numbands + 1;
            % contains the found band coordinates and their color in an n*3
            % matrix
            bands(numbands,1) = i;
            bands(numbands,[2 3]) = bandprops(i,colorprops(i,2),[1 2]);
        end
    end
end


% if only two solid are bands found, return with an error
if (numbands < 3),
    msgbox('Only two solid bands found.');
    return;
end

% sort the found band ceters by ascending x coordinate
[Zs idx] = sort(bands(:,2));
bands = bands(idx,:);

% determine the largest distance between two neighboring bands in the
% sorted list
largest_dist = 0;
dist = zeros(numbands-1,1);
for i=2:numbands,
    distance = sqrt((bands(i-1,2)-bands(i,2))^2 + (bands(i-1,3)-bands(i,3))^2);
    dist(i-1) = distance;
    if (distance > largest_dist),
        largest_dist = distance;
    end
end

% merge same color dots which are very close to each other compared to the
% largest distance
numbands_m = 1;
bands_m(1,:) = bands(1,:);
for i=2:numbands,
    if ((bands_m(numbands_m,1) == bands(i,1)) && ((sqrt((bands_m(numbands_m,2)-bands(i,2))^2 + (bands_m(numbands_m,3)-bands(i,3))^2)) < 0.2*largest_dist)  ),
        % merged bands:
        bands_m(numbands_m,[2 3]) = [mean([bands_m(numbands_m,2) bands(i,2)]) mean([bands_m(numbands_m,3) bands(i,3)])];
    else
        numbands_m = numbands_m + 1;
        bands_m(numbands_m,:) = bands(i,:);
    end
end

% the colors which are considered detected so far
numcolors = find(colorprops(:,1)>0);

% the coordinates of the center line of the resistor
if (size(Iresc,2) > size(Iresc,1))
    for i=1:size(Iresc,2),
        linecoord(i,[1 2]) = [i tan(alpha)*(i-mask_props.Centroid(1))+mask_props.Centroid(2)];
    end
else
    for i=1:size(Iresc,1),
        yc = size(Ith,1)-i;
        linecoord(i,[1 2]) = [mask_props.Centroid(1)+(i-mask_props.Centroid(2))/tan(alpha) i];
    end
end

% plotting the center line for testing
figure
imshow(Iresc)
hold all
for i=1:size(linecoord,1),
    plot(linecoord(i,1), size(Ith,1)-linecoord(i,2),'r.','MarkerSize',5);
end

% detection of possible silver or gold bands
C_distance = 0;
if ((numbands_m == 3) || (numbands == 4)),
    bands_centroid = [mean(bands(:,2)) mean(bands(:,3))];
    num_C_distance = 0;
    local_Cs = zeros(4,4,round(mask_props.MajorAxisLength*0.05));
    pixels_num = zeros(round(mask_props.MajorAxisLength*0.05),1);
    pixels = 0;
    if (abs(A) < pi/4)
        if (bands_centroid(1) < mask_props.Centroid(1))
            lowerlim = find(linecoord(:,1) > round(max(bands_m(:,2))), 1 );
            upperlim = size(linecoord,1);
        else
            lowerlim = 1;
            upperlim = find(linecoord(:,1) < round(min(bands_m(:,2))), 1, 'last' );
        end
    else
        if (bands_centroid(2) < mask_props.Centroid(2))
            lowerlim = find(linecoord(:,2) > round(max(bands_m(:,3))), 1 );
            upperlim = size(linecoord,1);
        else
            lowerlim = 1;
            upperlim = find(linecoord(:,2) < round(min(bands_m(:,3))), 1, 'last' );
        end
    end 
    for i=lowerlim:upperlim,
        basecoord = linecoord(i,:); % the coordinate of the middle line from which the perpendicular lines extend
        num_local_pixels = 0;   % counter which stores how many pixels we sample at a given basecoord
        local_pixels = 0;   % the array with the locally samplet pixels
        for j=0:size(pixels_num,1)-2,
            pixels_num(size(pixels_num,1)-j) = pixels_num(size(pixels_num,1)-j-1);
        end
        for j=1:round(mask_props.MinorAxisLength*0.5),
            if (abs(A) < pi/4)
                coorda = [basecoord(1)+j/tan(alpha+pi/2) j+basecoord(2)];
                coordb = [basecoord(1)-j/tan(alpha+pi/2) basecoord(2)-j];
            else
                coorda = [j+basecoord(1) tan(alpha+pi/2)*j+basecoord(2)];
                coordb = [basecoord(1)-j tan(alpha+pi/2)*(-j)+basecoord(2)];
            end
            if ((round(coorda(1)) > 0) && (round(coorda(1)) <= size(Ith,2)) && (size(Ith,1)-round(coorda(2)) > 0) && (size(Ith,1)-round(coorda(2))<=size(Ith,1)) && (round(coordb(1)) > 0) && (round(coordb(1)) <= size(Ith,2)) && (size(Ith,1)-round(coordb(2)) > 0) && (size(Ith,1)-round(coordb(2))<=size(Ith,1)))
                if ((sum(Iresc(size(Ith,1)-round(coorda(2)),round(coorda(1)),:))) && (sum(Iresc(size(Ith,1)-round(coordb(2)),round(coordb(1)),:))))
                    num_local_pixels = num_local_pixels + 1;
                    d = round(double(abs(A*coorda(1)+B*(size(Ith,1)-coorda(2))+C)/sqrt(A^2+B^2))/mask_props.MajorAxisLength*300.0);
                    local_pixels((num_local_pixels-1)*2+1,[1 2 3 4]) = [Iresc(size(Ith,1)-round(coorda(2)),round(coorda(1)),1) Iresc(size(Ith,1)-round(coorda(2)),round(coorda(1)),2) Iresc(round(size(Ith,1)-coorda(2)),round(coorda(1)),3) d];
                    local_pixels((num_local_pixels-1)*2+2,[1 2 3 4]) = [Iresc(size(Ith,1)-round(coordb(2)),round(coordb(1)),1) Iresc(size(Ith,1)-round(coordb(2)),round(coordb(1)),2) Iresc(round(size(Ith,1)-coordb(2)),round(coordb(1)),3) d];
                    plot(coorda(1), size(Ith,1)-coorda(2),'r.','MarkerSize',5);
                    plot(coordb(1), size(Ith,1)-coordb(2),'r.','MarkerSize',5);
                end
            end
        end
        if (local_pixels)
            pixels_num(1) = size(local_pixels,1);
        else
            pixels_num(1) = 0;
        end
        if (pixels)
            if (pixels_num(size(pixels_num,1)))
                pixels(sum(pixels_num(1:size(pixels_num,1)-1))+1:size(pixels,1),:) = [];
            end
            if (pixels_num(1))
                pixels = vertcat(local_pixels, pixels);
            end
        else
            pixels = local_pixels;
        end
        local_C = zeros(4,4);
%         for k=0:size(local_Cs,3)-2,
%             local_Cs(:,:,size(local_Cs,3)-k) = local_Cs(:,:,size(local_Cs,3)-k-1);
%         end
%         if (sum(Iresc(size(Ith,1)-round(basecoord(2)),round(basecoord(1)),:)) && (size(local_pixels,1)>30))
%             local_pixels(size(local_pixels,1)+1,[1 2 3 4]) = [Iresc(round(basecoord(2)),round(basecoord(1)),1) Iresc(round(basecoord(2)),round(basecoord(1)),2) Iresc(round(basecoord(2)),round(basecoord(1)),3) 0];
%             local_Cs(:,:,1) = cov(double(local_pixels));
%         else
%             local_Cs(:,:,1) = zeros(4,4);
%         end
%         local_C = mean(local_Cs,3);
        if (size(pixels,1)>80)
            local_C = cov(double(pixels(:,[1 2 3])));
        else
            local_C = zeros(3,3);
        end
        num_C_distance = num_C_distance + 1;
        C_distance(num_C_distance) = norm(gold_C-local_C)/norm(gold_C);
        if (C_distance(num_C_distance) < 0.45)
            plot(basecoord(1),size(Ith,1)-basecoord(2),'g.','MarkerSize',20);
            Ibands(size(Ith,1)-round(basecoord(2)),round(basecoord(1)),11) = 1;
        end
        basecoords(num_C_distance,[1 2]) = [basecoord(1) basecoord(2)];
    end
end
hold off

Ibands(:,:,11) = imdilate(Ibands(:,:,11),str_bands);
% Ibands(:,:,11) = imerode(Ibands(:,:,11),str_bands);

figure
if (exist('C_distance','var'))
    plot(C_distance);
end

gold_props = regionprops(Ibands(:,:,11),'Centroid');
gold_score = 0;
[gold_blobs,num_gold_blobs] = bwlabel(Ibands(:,:,11),8);
if (num_gold_blobs)
    for i=1:num_gold_blobs,
        blob_prp = regionprops(gold_blobs==i,'Centroid','Area');
        distance = norm(gold_props.Centroid - blob_prp.Centroid)+1;
        gold_score = gold_score + blob_prp.Area / distance * 10;
    end
end

if (gold_score > 12)
    cent = P*[gold_props.Centroid(1); size(Ith,1)-gold_props.Centroid(2)];
    bands_m(size(bands_m,1)+1,:) = [11 cent'];
    numbands_m = numbands_m + 1;
    [Zs idx] = sort(bands_m(:,2));
    bands_m = bands_m(idx,:);
end

% calculating the distances between detected solid bands
dist = zeros(numbands_m-1,1);
for i=1:numbands_m-1,
    dist(i) = sqrt((bands_m(i,2)-bands_m(i+1,2))^2 + (bands_m(i,3)-bands_m(i+1,3))^2);
end

% reorders the band list so that the outlying band is at the end of the
% list 
if ((bands_m(1,1) == 11) || (dist(1) > dist(numbands_m - 1)) && ~(bands_m(size(bands_m,1),1) == 11))
    [Zs idx] = sort(bands_m(:,2),'descend');
    bands_m = bands_m(idx,:);
end



% resistor value and tolerance
resvalue = zeros(2,1);
switch numbands_m
    case 4
        mult = 10^(bands_m(3));
        resvalue(1) = (bands_m(1)-1) * mult + (bands_m(2)-1) * mult/10;
        resvalue(2) = tolerances(bands_m(4));
    case 5
        mult = 10^(bands_m(4)+1);
        resvalue(1) = (bands_m(1)-1) * mult + (bands_m(2)-1) * mult/10 + (bands_m(3)-1) * mult/100;
        resvalue(2) = tolerances(bands_m(5));
end

%display results
figure
subplot(2,size(numcolors,1),[1 size(numcolors,1)])
imshow(Iresc);
hold all
text(1,1,[num2str(resvalue(1)) ' Ohms,+-' num2str(resvalue(2)) '%'],'BackgroundColor',[1 1 1]);
for i=1:numbands_m,
        if (i<numbands_m)
            plot(bands_m(i,2),size(Ith,1)-bands_m(i,3),'r.','MarkerSize',30);
        else
            plot(bands_m(i,2),size(Ith,1)-bands_m(i,3),'g.','MarkerSize',30);
        end
        text(bands_m(i,2),size(Ith,1)-bands_m(i,3),colortext(bands_m(i,1)),'BackgroundColor',[1 1 1]);
end
plot(mask_props.Centroid(1),size(Ith,1)-mask_props.Centroid(2),'r.','MarkerSize',30);
for i=1:size(Iresc,2),
    plot(i,size(Ith,1)-tan(alpha)*(i-mask_props.Centroid(1))-mask_props.Centroid(2), 'r.','MarkerSize',5);
end
hold off
for i=1:size(numcolors,1),
    subplot(2,size(numcolors,1),size(numcolors,1)+i);
    hold all
    imshow(Ibands(:,:,numcolors(i)));
    for j=1:colorprops(numcolors(i),2),
        plot(bandprops(numcolors(i),j,1),size(Ith,1)-bandprops(numcolors(i),j,2),'r.','MarkerSize',25);
    end
end



