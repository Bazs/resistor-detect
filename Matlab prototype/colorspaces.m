
hold all
pixels = double(brown_pixeldata);

mu = mean(pixels);
C = cov(double(pixels));
Cinv = sqrt(inv(C));

% for idx = 1:length(pixels),
%     pixels(idx,:) = (Cinv * pixels(idx,:)')' + mu;
% end


color = double(zeros(1, 3));
for idx = 1:length(pixels)
    color = [double(pixels(idx,1))/255.0 double(pixels(idx,2))/255.0 double(pixels(idx,3))/255.0];
%     stem3(pixels(idx,1), pixels(idx,2), pixels(idx,3), 'fill', 'Color', color, 'MarkerSize', 4, 'LineStyle', 'none');
    stem3(pixels(idx,1), pixels(idx,2), pixels(idx,3), 'fill', 'Color', color, 'MarkerSize', 4);
end

% 
% color = mu/255.0;
% stem3(mu(1), mu(2), mu(3),'fill', 'Color', color, 'MarkerSize', 10);

view(45, 28);
grid on
axis square
xlim([0 255]);
ylim([0 255]);
zlim([0 255]);
%plot_gaussian_ellipsoid(mu, C, 2);
%plotcov3(mu, C, 'surf-opts', {'EdgeAlpha', 0.5, 'FaceAlpha', 0.3, 'FaceColor', mu/255})
set(gca,'cameraviewanglemode','manual');
xlabel('Piros','FontSize',12,'FontWeight','bold','Color','r');
ylabel('Zöld','FontSize',12,'FontWeight','bold','Color','g');
zlabel('Kék','FontSize',12,'FontWeight','bold','Color','b');
% 
% xlabel('L','FontSize',12,'FontWeight','bold');
% ylabel('a','FontSize',12,'FontWeight','bold');
% zlabel('b','FontSize',12,'FontWeight','bold');

%%
hold all
pixels = double(green_pixeldata);
original_color = pixels;
pixels = pixels_RGB2Lab(pixels);

original_mu = mean(original_color);
mu = mean(pixels);
C = cov(double(pixels));
Cinv = sqrt(inv(C));

% for idx = 1:length(pixels),
%     pixels(idx,:) = (Cinv * pixels(idx,:)')' + mu;
% end

% 
color = double(zeros(1, 3));
% for idx = 1:length(pixels)
%     color = [double(original_color(idx,1))/255.0 double(original_color(idx,2))/255.0 double(original_color(idx,3))/255.0];
%     stem3(pixels(idx,1), pixels(idx,2), pixels(idx,3), 'fill', 'Color', color, 'MarkerSize', 4, 'LineStyle', 'none');
% %     stem3(pixels(idx,1), pixels(idx,2), pixels(idx,3), 'fill', 'Color', color, 'MarkerSize', 4);
% end

% 
% color = mu/255.0;
% stem3(mu(1), mu(2), mu(3),'fill', 'Color', color, 'MarkerSize', 10);

view(130, 10);
grid on
axis square
xlim([0 255]);
ylim([0 255]);
zlim([0 255]);
%plot_gaussian_ellipsoid(mu, C, 2);
plotcov3(mu, C, 'surf-opts', {'EdgeAlpha', 0.3, 'FaceAlpha', 0.3, 'FaceColor', original_mu/255.0})
set(gca,'cameraviewanglemode','manual');
%daspect([1,1,1]);
% xlabel('Piros','FontSize',12,'FontWeight','bold','Color','r');
% ylabel('Zöld','FontSize',12,'FontWeight','bold','Color','g');
% zlabel('Kék','FontSize',12,'FontWeight','bold','Color','b');

xlabel('L','FontSize',12,'FontWeight','bold');
ylabel('a','FontSize',12,'FontWeight','bold');
zlabel('b','FontSize',12,'FontWeight','bold');
axis tight;
daspect([1,1,1]);

%%
pan0 = double(45);
pan1 = double(80);
tilt0 = double(28);
tilt1 = double(5);
steps = 90;


pan = (pan0:((pan1-pan0)/(steps-1)):pan1)';
tilt = (tilt0:((tilt1-tilt0)/(steps-1)):tilt1)';
ViewZ = double(zeros(steps,2));
ViewZ(:,1) = pan;
ViewZ(:,2) = tilt;

OptionZ.FrameRate = 10;
OptionZ.Duration = 3.5;
OptionZ.Periodic = true;

set(gcf,'Renderer','zbuffer');

daObj=VideoWriter('rotationVid','MPEG-4');
open(daObj);

for kathy=1:size(ViewZ,1)
    view(ViewZ(kathy,:));
    writeVideo(daObj,getframe(gcf)); %use figure, since axis changes size based on view
end

close(daObj);