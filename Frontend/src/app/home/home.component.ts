import { Component, OnInit } from '@angular/core';

declare var $: any;

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html'
})
export class HomeComponent implements OnInit {

  ngOnInit(): void {
    // Wait until Angular has rendered the DOM
    setTimeout(() => this.initPlugins(), 300);
  }

  private initPlugins(): void {
    // Owl Carousel - Banner
    if ($('.owl-banner').length) {
      $('.owl-banner').owlCarousel({
        center: true,
        items: 1,
        loop: true,
        nav: true,
        navText: ['<i class="fa fa-angle-left"></i>', '<i class="fa fa-angle-right"></i>'],
        margin: 30
      });
    }

    // Preloader
    $('#js-preloader').addClass('loaded');

    // Sticky header on scroll
    $(window).off('scroll.header').on('scroll.header', function () {
      const scroll = $(window).scrollTop();
      const box = $('.header-text').height() || 400;
      const header = $('header').height() || 80;
      if (scroll >= box - header) {
        $('header').addClass('background-header');
      } else {
        $('header').removeClass('background-header');
      }
    });

    // Mobile menu trigger
    $('.menu-trigger').off('click').on('click', function (this: HTMLElement) {
      $(this).toggleClass('active');
      $('.header-area .nav').slideToggle(200);
    });
  }
}
